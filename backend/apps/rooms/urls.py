from rest_framework.routers import DefaultRouter

from .views import RoomViewSet, SeatViewSet, TimeSlotViewSet

router = DefaultRouter()
router.register('rooms', RoomViewSet)
router.register('seats', SeatViewSet)
router.register('timeslots', TimeSlotViewSet)

urlpatterns = router.urls
