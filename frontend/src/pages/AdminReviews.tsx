import { useAdminReviews, useApproveReview, useRejectReview, useHideReview, useRestoreReview } from '../features/reviews/hooks/useReviewAdmin';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { Badge } from '../components/ui/Badge';

const AdminReviews = () => {
  const { data, isLoading } = useAdminReviews();
  const approveMutation = useApproveReview();
  const rejectMutation = useRejectReview();
  const hideMutation = useHideReview();
  const restoreMutation = useRestoreReview();

  const reviews = data?.content || data || [];

  const handleApprove = (id) => {
    approveMutation.mutate(id);
  };

  const handleReject = (id) => {
    const reason = prompt('Reason for rejection:');
    if (reason) {
      rejectMutation.mutate({ id, reason });
    }
  };

  if (isLoading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Review Moderation</h1>
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Product</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">User</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Rating</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Content</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase">Status</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {reviews.map(r => (
              <tr key={r.id}>
                <td className="px-6 py-4 text-sm text-gray-900 font-mono">{r.productId?.substring(0, 8)}...</td>
                <td className="px-6 py-4 text-sm text-gray-900">{r.userId}</td>
                <td className="px-6 py-4 text-sm font-bold text-yellow-500">{'★'.repeat(r.rating)}</td>
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{r.title}</div>
                  <div className="text-sm text-gray-500 line-clamp-2">{r.content}</div>
                </td>
                <td className="px-6 py-4">
                  <Badge tone={r.status === 'APPROVED' ? 'success' : r.status === 'REJECTED' ? 'danger' : 'warning'}>{r.status}</Badge>
                </td>
                <td className="px-6 py-4 text-right flex gap-1 justify-end">
                  {r.status === 'PENDING' && (
                    <>
                      <Button size="xs" variant="outline" onClick={() => handleApprove(r.id)} disabled={approveMutation.isPending}>Approve</Button>
                      <Button size="xs" variant="outline" onClick={() => handleReject(r.id)} disabled={rejectMutation.isPending}>Reject</Button>
                    </>
                  )}
                  {r.status === 'APPROVED' && (
                    <Button size="xs" variant="ghost" className="text-amber-600" onClick={() => hideMutation.mutate(r.id)} disabled={hideMutation.isPending}>Hide</Button>
                  )}
                  {r.status === 'HIDDEN' && (
                    <Button size="xs" variant="ghost" className="text-emerald-600" onClick={() => restoreMutation.mutate(r.id)} disabled={restoreMutation.isPending}>Restore</Button>
                  )}
                </td>
              </tr>
            ))}
            {!reviews.length && (
              <tr>
                <td colSpan="6" className="px-6 py-8 text-center text-gray-400">No reviews found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminReviews;
