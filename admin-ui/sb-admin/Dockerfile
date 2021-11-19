# production environment
FROM nginx:stable-alpine
COPY ./build/adminui /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]