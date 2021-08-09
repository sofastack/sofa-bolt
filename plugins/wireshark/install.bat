@echo off
:: Get personal plugin folder from https://www.wireshark.org/docs/wsug_html_chunked/ChPluginFolders.html
SET plugin_dir=%APPDATA%\Wireshark\plugins
SET script_dir=%cd%
if not exist %plugin_dir% mkdir %plugin_dir%
copy %script_dir%\bolt.lua %plugin_dir%
echo Wireshark bolt protocol extension has been installed successfully, please restart Wireshark before using it.