from rest_framework.routers import DefaultRouter

from .views import ScheduleViewSet

router = DefaultRouter()
router.register('schedules', ScheduleViewSet)

urlpatterns = router.urls
