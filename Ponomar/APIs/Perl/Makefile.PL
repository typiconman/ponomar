package MY;
use 5.014002;
use ExtUtils::MakeMaker;
use File::ShareDir::Install;

delete_share '.';
install_share dist => '../../languages';

my @scripts = grep {-f && !m/\./o && !m/~$/o } glob("scripts/*");

WriteMakefile(
    NAME              => 'Ponomar',
    VERSION_FROM      => 'Ponomar.pm', # finds $VERSION
    clean => { FILES => "doc/ponomar.tex doc/*.log doc/*.log doc/*.aux doc/*.pdf doc/*.toc doc/*.out doc/*.idx doc/*.glo" },
    PREREQ_PM         => { 'XML::Parser' => 0, 'YAML::Tiny' => 0 },
    EXE_FILES => \@scripts,
    ($] >= 5.005 ?     ## Add these new keywords supported since 5.005
      (ABSTRACT_FROM  => 'Ponomar.pm', # retrieve abstract from module
       AUTHOR         => 'Aleksandr Andreev <aleksandr.andreev@gmail.com>') : ()),
);

sub postamble {
    my $self = shift;

    my @ret = File::ShareDir::Install::postamble( $self );
    # ... add the documentation
    push @ret, 'docs : ponomar.pdf';
    push @ret, "ponomar.pdf :\n\t(cd doc; \$(MAKE) docs)";
    return join "\n", @ret;
}