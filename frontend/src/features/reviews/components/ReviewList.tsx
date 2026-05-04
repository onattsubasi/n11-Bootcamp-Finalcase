import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { Card } from '../../../components/ui/Card';
import { RatingStars } from '../../../components/ui/RatingStars';
import { Badge } from '../../../components/ui/Badge';
import { Spinner } from '../../../components/ui/Spinner';
import { formatDate } from '../../../lib/utils/format';
import { useStore } from '../../../store';
import { useCreateReview, useProductRatingSummary, useProductReviews } from '../hooks/useReviews';

export const ReviewList = ({ productId }) => {
  const isAuthenticated = useStore((state) => state.isAuthenticated);
  const [formOpen, setFormOpen] = useState(false);
  const [rating, setRating] = useState(5);
  const [title, setTitle] = useState('');
  const [comment, setComment] = useState('');
  const reviewsQuery = useProductReviews(productId, 0, 10);
  const summaryQuery = useProductRatingSummary(productId);
  const { mutate: createReview, isPending: isSubmitting } = useCreateReview();

  const summary = summaryQuery.data;
  const reviews = reviewsQuery.data?.items ?? reviewsQuery.data?.content ?? [];

  const handleSubmit = (event) => {
    event.preventDefault();

    createReview(
      {
        productId,
        rating,
        title,
        comment,
      },
      {
        onSuccess: () => {
          setRating(5);
          setTitle('');
          setComment('');
          setFormOpen(false);
        },
      }
    );
  };

  return (
    <section className="space-y-4 rounded-xl bg-white p-6 shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-semibold text-gray-900">Reviews</h2>
          {summary ? (
            <div className="mt-2 flex items-center gap-2 text-sm text-gray-600">
              <RatingStars value={summary.averageRating ?? 0} />
              <span className="font-semibold text-gray-900">{(summary.averageRating ?? 0).toFixed(1)}</span>
              <span>({summary.reviewCount ?? 0} reviews)</span>
            </div>
          ) : null}
        </div>

        {isAuthenticated ? (
          <Button type="button" variant="ghost" onClick={() => setFormOpen((current) => !current)}>
            {formOpen ? 'Cancel' : 'Write a review'}
          </Button>
        ) : null}
      </div>

      {isAuthenticated ? (
        formOpen ? (
          <Card className="p-4">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <span className="text-sm font-medium text-gray-700">Your rating</span>
                <RatingStars value={rating} size={24} interactive onChange={setRating} />
              </div>

              <label className="block space-y-2">
                <span className="text-sm font-medium text-gray-700">Title</span>
                <input
                  type="text"
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                  maxLength={150}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none transition focus:border-blue-500"
                  placeholder="Summarize your experience"
                />
              </label>

              <label className="block space-y-2">
                <span className="text-sm font-medium text-gray-700">Comment</span>
                <textarea
                  required
                  rows={4}
                  value={comment}
                  onChange={(event) => setComment(event.target.value)}
                  maxLength={5000}
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 outline-none transition focus:border-blue-500"
                  placeholder="What did you think about the product?"
                />
              </label>

              <div className="flex gap-2">
                <Button type="submit" variant="primary" disabled={isSubmitting}>
                  {isSubmitting ? 'Submitting...' : 'Submit review'}
                </Button>
                <Button type="button" variant="ghost" onClick={() => setFormOpen(false)}>
                  Cancel
                </Button>
              </div>
            </form>
          </Card>
        ) : null
      ) : (
        <p className="text-sm text-gray-500">Sign in to share your review.</p>
      )}

      {reviewsQuery.isPending ? (
        <div className="flex items-center justify-center py-8">
          <Spinner />
        </div>
      ) : reviews.length === 0 ? (
        <p className="text-sm text-gray-500">No reviews yet.</p>
      ) : (
        <div className="space-y-3">
          {reviews.map((review) => (
            <Card key={review.reviewId ?? review.id} className="p-4">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-semibold text-gray-900">{review.title || 'Review'}</span>
                    {review.verifiedPurchase ? <Badge tone="success">Verified purchase</Badge> : null}
                    {review.status ? <Badge tone="neutral">{review.status}</Badge> : null}
                  </div>
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <RatingStars value={review.rating ?? 0} size={14} />
                    <span>{review.comment ? 'Customer review' : 'Review'}</span>
                  </div>
                </div>
                <span className="text-xs text-gray-400">{formatDate(review.createdAt ?? new Date())}</span>
              </div>

              <p className="mt-3 whitespace-pre-wrap text-sm text-gray-700">{review.comment}</p>
            </Card>
          ))}
        </div>
      )}
    </section>
  );
};

export default ReviewList;