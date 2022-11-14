docker build -t kerma-rpanic .
docker run -d -p 18018:18018 -v ./config.yml:/app/config.yml -v ./data:/app/data kerma-rpanic