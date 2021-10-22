FROM node:12 as build

WORKDIR /usr/local/app

COPY ./ /usr/local/app/

RUN npm install
RUN npm run build:prod

FROM nginx
COPY --from=build /usr/local/app/dist /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/nginx.conf
EXPOSE 4200
CMD ["nginx", "-g", "daemon off;"]
