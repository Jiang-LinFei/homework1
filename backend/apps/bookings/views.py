from django.utils import timezone
from rest_framework import status, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from apps.accounts.permissions import IsAdminOrTeacher

from .conflicts import has_conflict
from .models import Booking
from .serializers import BookingSerializer


class BookingViewSet(viewsets.ModelViewSet):
    queryset = Booking.objects.select_related(
        'applicant', 'room', 'seat', 'timeslot', 'reviewer').all()
    serializer_class = BookingSerializer
    filterset_fields = ['status', 'room', 'date', 'applicant']
    search_fields = ['purpose', 'room__name']

    def get_queryset(self):
        qs = super().get_queryset()
        user = self.request.user
        if user.role == 'student':
            qs = qs.filter(applicant=user)
        return qs

    def perform_create(self, serializer):
        serializer.save(applicant=self.request.user)

    def _review(self, request, pk, new_status):
        booking = self.get_object()
        if booking.status != Booking.Status.PENDING:
            return Response({'detail': '该预约不是待审批状态'},
                            status=status.HTTP_400_BAD_REQUEST)
        if new_status == Booking.Status.APPROVED:
            start, end = booking.time_range
            if has_conflict(booking.room, booking.seat, booking.date, start, end,
                            exclude_id=booking.id):
                return Response({'detail': '通过失败：与其它已通过预约/排课冲突'},
                                status=status.HTTP_400_BAD_REQUEST)
        booking.status = new_status
        booking.reviewer = request.user
        booking.review_comment = request.data.get('comment', '')
        booking.reviewed_at = timezone.now()
        booking.save()
        return Response(BookingSerializer(booking).data)

    @action(detail=True, methods=['post'], permission_classes=[IsAdminOrTeacher])
    def approve(self, request, pk=None):
        return self._review(request, pk, Booking.Status.APPROVED)

    @action(detail=True, methods=['post'], permission_classes=[IsAdminOrTeacher])
    def reject(self, request, pk=None):
        return self._review(request, pk, Booking.Status.REJECTED)

    @action(detail=True, methods=['post'])
    def cancel(self, request, pk=None):
        booking = self.get_object()
        if booking.applicant_id != request.user.id and request.user.role != 'admin':
            return Response({'detail': '只能取消自己的预约'},
                            status=status.HTTP_403_FORBIDDEN)
        if booking.status not in (Booking.Status.PENDING, Booking.Status.APPROVED):
            return Response({'detail': '当前状态不可取消'},
                            status=status.HTTP_400_BAD_REQUEST)
        booking.status = Booking.Status.CANCELLED
        booking.save()
        return Response(BookingSerializer(booking).data)
