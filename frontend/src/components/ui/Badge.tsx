import { cn } from '../../lib/utils/cn';

const toneStyles = {
  neutral: 'bg-gray-100 text-gray-700',
  success: 'bg-emerald-100 text-emerald-800',
  warning: 'bg-amber-100 text-amber-800',
  danger: 'bg-red-100 text-red-800',
  info: 'bg-blue-100 text-blue-800',
};

export const Badge = ({ children, className = '', tone = 'neutral', ...props }) => {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold transition-colors',
        toneStyles[tone] ?? toneStyles.neutral,
        className
      )}
      {...props}
    >
      {children}
    </span>
  );
};

export default Badge;