docs: ponomar.pdf

ponomar.pdf:
	rm -f *.log *.aux *.pdf *.toc *.out *.idx *.glo ponomar.tex
	pod2latex -full -prefile h.tex -postfile f.tex -out ponomar.tex ../Ponomar.pm ../Ponomar/
	xelatex --interaction=nonstopmode ponomar.tex
	xelatex --interaction=nonstopmode ponomar.tex

clean:
	rm -f *.log *.aux *.pdf *.toc *.out *.idx *.glo ponomar.tex

