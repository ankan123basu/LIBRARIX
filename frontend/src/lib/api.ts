const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8085/api';

export function getStoredToken(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('librarix_jwt');
  }
  return null;
}

export function setStoredToken(token: string) {
  if (typeof window !== 'undefined') {
    localStorage.setItem('librarix_jwt', token);
  }
}

export function clearStoredToken() {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('librarix_jwt');
    localStorage.removeItem('librarix_user');
  }
}

export function getStoredUser(): any | null {
  if (typeof window !== 'undefined') {
    const raw = localStorage.getItem('librarix_user');
    return raw ? JSON.parse(raw) : null;
  }
  return null;
}

export async function fetchWithAuth(endpoint: string, options: RequestInit = {}) {
  const token = getStoredToken();
  const headers = new Headers(options.headers || {});
  
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  // 3.5-second timeout to prevent fetch hanging when unauthenticated or backend unreachable
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 3500);
  const signal = options.signal || controller.signal;

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
      signal,
    });
    clearTimeout(timeoutId);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'HTTP Request Failed' }));
      throw new Error(errorData.message || `Request failed with status ${response.status}`);
    }

    return await response.json();
  } catch (err) {
    clearTimeout(timeoutId);
    throw err;
  }
}
