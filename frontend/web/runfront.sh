#!/bin/bash

# Get node setup file
curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
wait
# Install node and build tools
sudo apt-get install -y nodejs build-essential
wait
# Symlink nodejs to node
ln -s /usr/bin/nodejs /usr/bin/node
# Install http-server
sudo npm install -g http-server ngrok
wait
# Run http-server to launch web frontend
http-server ./app &
sleep 1
# Launch ngrok tunnel to host over https
ngrok http 8080