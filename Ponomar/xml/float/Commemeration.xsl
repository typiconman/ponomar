<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
	<title>Commemeration</title>
  <body>
<xsl:apply-templates/>
</body>
</html>
</xsl:template>

<xsl:template match="LANGUAGE">
	<xsl:param name="input" select="NAME"/>
	<xsl:if test="not(string-length($input) = 0)">
		<p>    
		<h2><center><FONT color='red'><xsl:value-of select="NAME"/></FONT></center></h2>
		</p>
		<p><center><FONT color='red'>Readings for the saint</FONT></center></p>
		<xsl:for-each select="SCRIPTURE">
			<xsl:if test="not(string-length(@Type) = 0)">
				<p><center><FONT color='red'><xsl:value-of select="@Type"/>: <xsl:value-of select="@Reading"/></FONT></center></p>
			</xsl:if>
		</xsl:for-each>
		<p>
		<h3><center><FONT color='red'>Troparion/Тропарь (Tone/глас <xsl:value-of select="TROPARION/@Tone"/>)</FONT></center></h3>
    		<center><xsl:value-of select="TROPARION"/></center>
		<h3><center><FONT color='red'>Kontakion/Кондак (Tone/глас <xsl:value-of select="KONTAKION/@Tone"/>)</FONT></center></h3>
		<center><xsl:value-of select="KONTAKION"/></center>
		</p>
<center>--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- </center>
	</xsl:if>
 </xsl:template>


</xsl:stylesheet>
