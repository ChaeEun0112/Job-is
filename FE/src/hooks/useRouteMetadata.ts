import { useEffect } from 'react';
import { useMatches } from 'react-router';

export interface RouteMetadata {
  title: string;
  description: string;
  robots?: string;
}

const DEFAULT_METADATA: Required<RouteMetadata> = {
  title: 'Job.is | 매일 아침, 맞춤 취업 뉴스레터',
  description: '오늘 들어온 공고 중 나에게 맞는 채용 정보를 매일 아침 편지로 받아보세요.',
  robots: 'index,follow',
};

function isRouteMetadata(value: unknown): value is RouteMetadata {
  return (
    typeof value === 'object' &&
    value !== null &&
    'title' in value &&
    typeof value.title === 'string' &&
    'description' in value &&
    typeof value.description === 'string'
  );
}

function setMeta(attribute: 'name' | 'property', key: string, content: string) {
  const metadata = document.head.querySelector<HTMLMetaElement>(`meta[${attribute}="${key}"]`);

  if (metadata) {
    metadata.content = content;
    return;
  }

  const nextMetadata = document.createElement('meta');
  nextMetadata.setAttribute(attribute, key);
  nextMetadata.content = content;
  document.head.append(nextMetadata);
}

function setCanonicalUrl(url: string) {
  const canonical = document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');

  if (canonical) {
    canonical.href = url;
    return;
  }

  const nextCanonical = document.createElement('link');
  nextCanonical.rel = 'canonical';
  nextCanonical.href = url;
  document.head.append(nextCanonical);
}

export default function useRouteMetadata() {
  const matches = useMatches();
  const metadata =
    [...matches]
      .reverse()
      .map((match) => (match.handle as { seo?: unknown } | undefined)?.seo)
      .find(isRouteMetadata) ?? DEFAULT_METADATA;

  useEffect(() => {
    const canonicalUrl = `${window.location.origin}${window.location.pathname}`;

    document.title = metadata.title;
    setMeta('name', 'description', metadata.description);
    setMeta('name', 'robots', metadata.robots ?? DEFAULT_METADATA.robots);
    setMeta('property', 'og:title', metadata.title);
    setMeta('property', 'og:description', metadata.description);
    setMeta('property', 'og:url', canonicalUrl);
    setMeta('name', 'twitter:title', metadata.title);
    setMeta('name', 'twitter:description', metadata.description);
    setCanonicalUrl(canonicalUrl);
  }, [metadata]);
}
