import { NextRequest, NextResponse } from 'next/server';

// Pages that require specific access levels
const RESTRICTED_PAGES = {
  '/wizard': 'wizard',
  '/admin': 'admin',
  '/test': 'redirect', // Always redirect to dashboard
};

// Pages that are always accessible
const PUBLIC_PAGES = [
  '/login',
  '/register',
  '/dashboard',
  '/subarea',
  '/', // Landing page
];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const locale = pathname.split('/')[1];
  
  // Check if this is a locale path
  const isLocalePath = /^\/[a-z]{2}\//.test(pathname);
  const basePath = isLocalePath ? pathname.substring(3) : pathname;
  
  // Handle test page redirect
  if (basePath === '/test') {
    const dashboardUrl = isLocalePath 
      ? `/${locale}/dashboard` 
      : '/dashboard';
    return NextResponse.redirect(new URL(dashboardUrl, request.url));
  }

  // For other restricted pages, let the client-side AccessGuard handle the protection
  // This middleware only handles immediate redirects like the test page
  
  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
}; 