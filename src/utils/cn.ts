import { clsx, type ClassValue } from 'clsx';
import { extendTailwindMerge } from 'tailwind-merge';

const twMerge = extendTailwindMerge({
  extend: {
    classGroups: {
      'font-size': [
        {
          text: [
            'display-small',
            'display-medium',
            'display-large',
            'heading-xxsmall',
            'heading-xsmall',
            'heading-small',
            'heading-medium',
            'heading-large',
            'heading-xlarge',
            'body-xsmall',
            'body-small',
            'body-medium',
            'body-large',
            'label-xsmall',
            'label-small',
            'label-medium',
            'label-large',
            'nav-small',
            'nav-medium',
            'nav-large',
          ],
        },
      ],
    },
  },
});

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
