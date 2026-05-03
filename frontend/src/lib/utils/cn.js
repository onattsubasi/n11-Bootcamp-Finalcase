import clsx from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Combines classnames with tailwind-merge to avoid conflicts.
 * Useful for component className overrides.
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}
