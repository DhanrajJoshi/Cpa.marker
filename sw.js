// Service Worker for offline support and caching
const CACHE_NAME = 'dhanraj-portfolio-v1';
const urlsToCache = [
  '/',
  '/index.htm',
  '/frontend/index.html',
  '/frontend/style.css',
  '/frontend/app.js',
  '/styles.css',
  '/script.js',
  '/manifest.json'
];

// Install event - cache assets
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Cache opened');
        return cache.addAll(urlsToCache);
      })
      .catch(error => console.log('Cache error:', error))
  );
  self.skipWaiting();
});

// Activate event - clean up old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  self.clients.claim();
});

// Fetch event - serve from cache, fallback to network
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') {
    return;
  }

  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // Cache hit - return response
        if (response) {
          return response;
        }

        return fetch(event.request).then(response => {
          // Check if valid response
          if (!response || response.status !== 200 || response.type === 'error') {
            return response;
          }

          // Clone the response
          const responseToCache = response.clone();
          caches.open(CACHE_NAME)
            .then(cache => {
              cache.put(event.request, responseToCache);
            });

          return response;
        });
      })
      .catch(() => {
        // Network request failed, try to return a cached page
        return caches.match('/index.htm');
      })
  );
});

// Handle background sync (for contact form)
self.addEventListener('sync', event => {
  if (event.tag === 'sync-contact-form') {
    event.waitUntil(syncContactForm());
  }
});

async function syncContactForm() {
  try {
    const response = await fetch('/api/contact', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(
        JSON.parse(localStorage.getItem('pendingContactForm') || '{}')
      )
    });
    if (response.ok) {
      localStorage.removeItem('pendingContactForm');
    }
  } catch (error) {
    console.error('Sync failed:', error);
  }
}
