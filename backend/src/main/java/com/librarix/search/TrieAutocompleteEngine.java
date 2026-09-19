package com.librarix.search;

import com.librarix.model.Resource;
import com.librarix.repository.ResourceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Trie-Based Autocomplete Search Engine
 *
 * DSA: Trie (prefix tree) with DFS traversal for top-K suggestion collection.
 *
 * Time Complexity:
 *   - Insert: O(m) where m = word length
 *   - Autocomplete: O(p + k) where p = prefix length, k = result limit
 *   - vs MongoDB regex "^prefix.*": O(n) full collection scan
 *
 * Rebuilt on resource create/update/delete to stay consistent with the catalog.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrieAutocompleteEngine {

    private final ResourceRepository resourceRepository;

    private TrieNode root = new TrieNode();
    private final Map<String, String> wordToResourceId = new HashMap<>();

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
        String fullWord = null;
        int frequency = 0; // Higher frequency = more popular suggestion
    }

    @PostConstruct
    public void buildTrieFromCatalog() {
        rebuildTrie();
    }

    /**
     * Rebuild the entire trie from the current catalog.
     * Called on startup and after any resource create/update/delete.
     */
    public synchronized void rebuildTrie() {
        TrieNode newRoot = new TrieNode();
        Map<String, String> newMap = new HashMap<>();

        List<Resource> resources = resourceRepository.findAll();

        for (Resource resource : resources) {
            // Index title words
            if (resource.getTitle() != null) {
                insertPhrase(newRoot, resource.getTitle().toLowerCase(), resource.getId(), newMap);
            }
            // Index author/brand words
            if (resource.getAuthorOrBrand() != null) {
                insertPhrase(newRoot, resource.getAuthorOrBrand().toLowerCase(), resource.getId(), newMap);
            }
            // Index tags
            if (resource.getTags() != null) {
                for (String tag : resource.getTags()) {
                    insertWord(newRoot, tag.toLowerCase().replaceAll("[^a-z0-9]", ""), resource.getId(), newMap);
                }
            }
        }

        this.root = newRoot;
        this.wordToResourceId.clear();
        this.wordToResourceId.putAll(newMap);
        log.info("Trie rebuilt from catalog: {} resources indexed", resources.size());
    }

    private void insertPhrase(TrieNode root, String phrase, String resourceId, Map<String, String> map) {
        String[] words = phrase.split("\\s+");
        for (String word : words) {
            String clean = word.replaceAll("[^a-z0-9]", "");
            if (clean.length() >= 2) {
                insertWord(root, clean, resourceId, map);
            }
        }
        // Also insert the full phrase as one entry
        String fullClean = phrase.replaceAll("[^a-z0-9\\s]", "").trim();
        if (fullClean.length() >= 2) {
            insertWord(root, fullClean, resourceId, map);
        }
    }

    private void insertWord(TrieNode root, String word, String resourceId, Map<String, String> map) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
        current.fullWord = word;
        current.frequency++;
        map.put(word, resourceId);
    }

    /**
     * Autocomplete: find all words starting with the given prefix.
     * Uses DFS from the prefix node to collect up to `limit` suggestions.
     *
     * @param prefix Search prefix (e.g., "dist")
     * @param limit  Max number of suggestions to return
     * @return List of matching words, sorted by frequency descending
     */
    public List<String> autocomplete(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        String cleanPrefix = prefix.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        TrieNode current = root;

        // Navigate to the prefix node
        for (char c : cleanPrefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return List.of(); // No matches for this prefix
            }
            current = current.children.get(c);
        }

        // DFS to collect all words under this prefix
        List<String> results = new ArrayList<>();
        PriorityQueue<String[]> pq = new PriorityQueue<>(
                Comparator.comparingInt((String[] a) -> Integer.parseInt(a[1])).reversed()
        );
        collectWords(current, pq);

        int count = 0;
        while (!pq.isEmpty() && count < limit) {
            results.add(pq.poll()[0]);
            count++;
        }

        return results;
    }

    private void collectWords(TrieNode node, PriorityQueue<String[]> pq) {
        if (node.isEndOfWord && node.fullWord != null) {
            pq.offer(new String[]{node.fullWord, String.valueOf(node.frequency)});
        }
        for (TrieNode child : node.children.values()) {
            collectWords(child, pq);
        }
    }

    /**
     * Get the resource ID associated with a word (for linking suggestions to books).
     */
    public String getResourceIdForWord(String word) {
        return wordToResourceId.get(word.toLowerCase());
    }

    public int getTrieSize() {
        return wordToResourceId.size();
    }
}
