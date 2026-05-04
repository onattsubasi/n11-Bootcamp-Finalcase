const TRY_FORMATTER = new Intl.NumberFormat('tr-TR', {
  style: 'currency',
  currency: 'TRY',
  maximumFractionDigits: 2,
});

/** Formats a number as Turkish Lira. */
export const formatTRY = (n: number): string => TRY_FORMATTER.format(n);
export const formatCurrency = formatTRY;

/** Formats an ISO date string or Date object to a long Turkish date (e.g., 20 Nisan 2024). */
export const formatDate = (iso: string | Date | undefined): string => {
  if (!iso) return '-';
  const d = typeof iso === 'string' ? new Date(iso) : iso;
  return d.toLocaleDateString('tr-TR', { day: '2-digit', month: 'long', year: 'numeric' });
};

/** Formats an ISO date string or Date object to a Turkish date-time. */
export const formatDateTime = (iso: string | Date | undefined): string => {
  if (!iso) return '-';
  const d = typeof iso === 'string' ? new Date(iso) : iso;
  return d.toLocaleString('tr-TR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};
