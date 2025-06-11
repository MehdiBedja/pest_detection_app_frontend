from django.urls import path, re_path
from . import views

urlpatterns = [
    path('login/', views.login),
    path('signup/', views.signup),
    path('get_user_info/', views.get_user_info),
    path('change_password/', views.change_password),
    path('logout/', views.logout),
    path('google/signup/', views.google_signup),
    path('google/login/', views.google_login),
    path('getUser/<int:id>/', views.custom_user_detail, name='customuser-detail'),
] 