#!/bin/bash

plugin_dir=~/.config/wireshark/plugins
script_dir=`dirname $0`
mkdir -p ${plugin_dir}
cp $script_dir/bolt.lua  ${plugin_dir}
echo "Wireshark bolt protocol extension has been installed successfully, please restart Wireshark before using it."