#!/usr/bin/env bash

BACKEND_PATHS=(backend ':(exclude)backend/kustomize' ':(exclude)backend/*.md' ':(exclude)backend/docker-compose.yaml')
FRONTEND_PATHS=(frontend ':(exclude)frontend/kustomize' ':(exclude)frontend/*.md')
