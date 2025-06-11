import logging
logger = logging.getLogger(__name__)

@api_view(['POST'])
def google_auth(request):
    logger.info("Received Google authentication request")
    try:
        id_token = request.data.get('id_token')
        if not id_token:
            logger.error("No ID token provided in request")
            return Response({'error': 'No ID token provided'}, status=status.HTTP_400_BAD_REQUEST)

        logger.debug("Verifying Google ID token")
        try:
            # Verify the token
            idinfo = id_token.verify_oauth2_token(id_token, requests.Request(), GOOGLE_CLIENT_ID)
            logger.info(f"Token verified successfully for user: {idinfo['email']}")

            # Get or create user
            email = idinfo['email']
            try:
                user = User.objects.get(email=email)
                logger.info(f"Existing user found: {email}")
            except User.DoesNotExist:
                logger.info(f"Creating new user for: {email}")
                user = User.objects.create_user(
                    username=email,
                    email=email,
                    password=get_random_string(20)  # Random password for security
                )

            # Generate JWT token
            logger.debug("Generating JWT token")
            token = generate_jwt_token(user)
            logger.info(f"JWT token generated successfully for user: {email}")

            return Response({
                'token': token,
                'user': {
                    'id': user.id,
                    'email': user.email,
                    'username': user.username
                }
            })

        except ValueError as e:
            logger.error(f"Token verification failed: {str(e)}")
            return Response({'error': 'Invalid token'}, status=status.HTTP_401_UNAUTHORIZED)

    except Exception as e:
        logger.error(f"Unexpected error in Google authentication: {str(e)}", exc_info=True)
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR) 