import { formatDateTime } from '../../lib/utils/format';

export const StatusTimeline = ({ history }) => {
  if (!history || history.length === 0) return null;

  return (
    <div className="flow-root">
      <ul className="-mb-8">
        {history.map((item, idx) => (
          <li key={idx}>
            <div className="relative pb-8">
              {idx !== history.length - 1 ? (
                <span className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200" aria-hidden="true" />
              ) : null}
              <div className="relative flex space-x-3">
                <div>
                  <span className="h-8 w-8 rounded-full bg-blue-500 flex items-center justify-center ring-8 ring-white">
                    <div className="h-2.5 w-2.5 rounded-full bg-white" />
                  </span>
                </div>
                <div className="flex min-w-0 flex-1 justify-between space-x-4 pt-1.5">
                  <div>
                    <p className="text-sm text-gray-500">
                      Changed to <span className="font-medium text-gray-900">{item.toStatus?.replace(/_/g, ' ')}</span>
                      {item.source ? ` via ${item.source}` : ''}
                    </p>
                    {item.reason && (
                      <p className="mt-1 text-xs text-red-500 italic">"{item.reason}"</p>
                    )}
                  </div>
                  <div className="whitespace-nowrap text-right text-sm text-gray-500">
                    <time dateTime={item.timestamp}>{formatDateTime(item.timestamp)}</time>
                  </div>
                </div>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};
