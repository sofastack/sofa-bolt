#!/bin/bash

plugin_dir=~/.config/wireshark/plugins
mkdir -p ${plugin_dir}
cp ./bolt.lua  ${plugin_dir}