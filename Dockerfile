FROM scratch
COPY build/native/nativeCompile/liteoss /app/liteoss
CMD ["/app/liteoss"]