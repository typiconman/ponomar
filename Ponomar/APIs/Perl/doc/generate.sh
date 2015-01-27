#!/bin/bash

pod2latex -full -prefile h.tex -postfile f.tex -out ponomar.tex ../Ponomar.pm ../Ponomar/
pdflatex ponomar.tex
pdflatex ponomar.tex


