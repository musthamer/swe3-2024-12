#!/bin/bash
echo " Installiere Git Hooks..."

# Pre-Commit Hook
cp hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

# Commit-Message Hook
cp hooks/commit-msg .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg

# Pre-Receive Hook (Serverseitig)
#scp ./hooks/pre-receive mohalzubaidy@hopper:/home/gitterbett/swe3-2024-12/hooks/pre-receive
#ssh mohalzubaidy@hopper "chmod +x /home/gitterbett/swe3-2024-12/hooks/pre-receive"

# Post-Receive Hook (Serverseitig)
#scp ./hooks/post-receive mohalzubaidy@hopper:/home/gitterbett/swe3-2024-12/hooks/post-receive
#ssh mohalzubaidy@hopper "chmod +x /home/gitterbett/swe3-2024-12/hooks/post-receive"

echo " Hooks erfolgreich installiert!"

