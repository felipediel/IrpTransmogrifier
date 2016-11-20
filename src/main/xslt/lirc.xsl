<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:transform
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
    xmlns:exportformats="http://www.harctoolbox.org/exportformats"
    xmlns:harctoolbox="xxxxxxxxx"
    version="2.0">

    <xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

    <xsl:output method="xml" />

    <xsl:param name="eps" select="'30'"/>
    <xsl:param name="aeps" select= "'100'"/>

    <xsl:function name="harctoolbox:canonical-name">
        <xsl:param name="str"/>
        <xsl:value-of select="replace(lower-case($str), '[^_0-9a-z-]', '-')"/>
    </xsl:function>

    <!-- default template for elements is to just ignore -->
    <xsl:template match="*"/>

    <xsl:template match="/">
        <exportformats:exportformat>
            <xsl:attribute name="name">Lirc</xsl:attribute>
            <xsl:attribute name="extension">lircd.conf</xsl:attribute>
            <xsl:attribute name="multiSignal">true</xsl:attribute>
            <xsl:attribute name="simpleSequence">false</xsl:attribute>
            <xsl:attribute name="metadata">true</xsl:attribute>
            <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">http://www.harctoolbox.org/exportformats http://www.harctoolbox.org/schemas/exportformats.xsd</xsl:attribute>

            <axsl:stylesheet>
                <xsl:namespace name="girr" select="'http://www.harctoolbox.org/Girr'"/>
                <xsl:namespace name="exporterutils" select="'http://xml.apache.org/xalan/java/org.harctoolbox.irscrutinizer.exporter.ExporterUtils'"/>
                <xsl:attribute name="version">1.0</xsl:attribute>

                <axsl:output method="text" />

            <axsl:template>
                <xsl:attribute name="match">/girr:remotes</xsl:attribute>
                <axsl:text xml:space="preserve"># </axsl:text>
                <axsl:value-of select="@title"/>
                <axsl:text>
#
# Creating tool: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$creatingTool</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Creating user: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$creatingUser</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Creating date: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$creatingDate</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Encoding: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">$encoding</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
#
</axsl:text>
                <axsl:apply-templates>
                    <xsl:attribute name="select">girr:remote</xsl:attribute>
                </axsl:apply-templates>
            </axsl:template>

            <axsl:template>
                <xsl:attribute name="match">girr:remote</xsl:attribute>
                <axsl:text># Manufacturer: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@manufacturer</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Model: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@model</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Displayname: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@displayName</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
# Remotename: </axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">@remoteName</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
#
</axsl:text>
                <axsl:apply-templates>
                    <xsl:attribute name="select">girr:commandSet</xsl:attribute>
                </axsl:apply-templates>
            </axsl:template>

            <!-- General case, raw codes -->
            <xsl:text xml:space="preserve">&#10;&#10;</xsl:text>
            <xsl:comment> ################ Default protocol rule, raw codes ############## </xsl:comment>
            <xsl:text xml:space="preserve">&#10;</xsl:text>
            <axsl:template>
                <xsl:attribute name="match">girr:commandSet</xsl:attribute>
                <axsl:text>begin remote
&#9;name&#9;&#9;</axsl:text>
                <axsl:value-of>
                    <xsl:attribute name="select">../@name</xsl:attribute>
                </axsl:value-of>
                <axsl:text>
&#9;flags&#9;&#9;RAW_CODES
&#9;eps&#9;&#9;30
&#9;aeps&#9;&#9;100
&#9;frequency&#9;</axsl:text>
        <axsl:value-of>
            <xsl:attribute name="select">//girr:command[1]/girr:raw/@frequency</xsl:attribute>
        </axsl:value-of>
        <axsl:text>
&#9;gap&#9;&#9;</axsl:text>
        <axsl:value-of select="//girr:command[1]/girr:raw/girr:repeat/girr:gap[position()=last()]"/>
        <axsl:text>
&#9;begin raw_codes
</axsl:text>
        <axsl:apply-templates select="//girr:command"/>
        <axsl:text>&#9;end raw_codes
end remote
</axsl:text>
    </axsl:template>

    <axsl:template match="girr:command">
        <axsl:text>&#9;&#9;name </axsl:text>
        <axsl:value-of select="@name"/>
        <axsl:text xml:space="preserve">
</axsl:text>
        <axsl:apply-templates select="girr:raw[1]"/>
        <axsl:text xml:space="preserve">
</axsl:text>
    </axsl:template>

    <axsl:template match="girr:raw">
        <axsl:apply-templates select="girr:intro"/>
        <axsl:if test="not(girr:intro)">
            <axsl:apply-templates select="girr:repeat"/>
        </axsl:if>
    </axsl:template>

    <axsl:template match="girr:intro|girr:repeat">
        <axsl:text xml:space="preserve">&#9;&#9;&#9;</axsl:text>
        <axsl:apply-templates select="*"/>
    </axsl:template>

    <axsl:template match="girr:flash">
        <axsl:value-of select="."/>
        <axsl:text xml:space="preserve"> </axsl:text>
    </axsl:template>

    <axsl:template match="girr:gap">
        <axsl:value-of select="."/>
        <axsl:text xml:space="preserve"> </axsl:text>
    </axsl:template>

    <axsl:template match="girr:gap[position() mod 4 = 0]">
        <axsl:value-of select="."/>
        <axsl:text xml:space="preserve">
&#9;&#9;&#9;</axsl:text>
    </axsl:template>

    <axsl:template match="girr:gap[position()=last()]"/>


    <xsl:apply-templates select="NamedProtocols/NamedProtocol"/>
            </axsl:stylesheet>
        </exportformats:exportformat>
    </xsl:template>

    <xsl:template match="NamedProtocol">
        <xsl:apply-templates select="Protocol"/>
    </xsl:template>

    <!-- Have not implemented definitions yet -->
    <xsl:template match="NamedProtocol[Protocol/Definitions/Definition]" priority="11">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: Definitions not yet implemented </xsl:text>
        </xsl:comment>
    </xsl:template>

    <!-- Protocol with assignments do not fit into Lirc -->
    <xsl:template match="NamedProtocol[Protocol[.//Assignment and not(@toggle='true')]]" priority="10">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: Assignment cannot be done in the Lirc framework </xsl:text>
        </xsl:comment>
    </xsl:template>

    <!-- Protocol with assignments do not fit into Lirc -->
    <!--xsl:template match="NamedProtocol[Protocol/BitspecIrstream[@interleavingOk='false']]" priority="9">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: interleavingOk is false </xsl:text>
        </xsl:comment>
    </xsl:template-->

    <!-- Protocol pwm4 -->
    <xsl:template match="NamedProtocol[Protocol[@pwm4='true']]">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: pwm4 not yet implemented </xsl:text>
        </xsl:comment>
    </xsl:template>

    <!-- Expressions as bitfields not implemented yet -->
    <xsl:template match="NamedProtocol[.//Data/Expression]">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: Expressions as bitfields not implemented yet </xsl:text>
        </xsl:comment>
    </xsl:template>

    <!-- Variations not implemented yet -->
    <xsl:template match="NamedProtocol[.//Variation]">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: Variations not implemented yet </xsl:text>
        </xsl:comment>
    </xsl:template>

    <!-- Expressions as bitfields not implemented yet -->
    <xsl:template match="NamedProtocol[Protocol[not(@standardPwm='true')
                                                and not(@pwm4='true')
                                                and not(@biphase='true')]]" priority="1">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: not one of the simple types (pwm, pwm4, biphase) </xsl:text>
        </xsl:comment>
    </xsl:template>

    <!-- Expressions as finitely repeating sequences not implemented -->
    <xsl:template match="NamedProtocol[.//IrStream[number(@repeatMin) > 1]]">
        <xsl:comment>
            <xsl:text> Protocol </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text> omitted: finite repeats in IrSequences not implemented </xsl:text>
        </xsl:comment>
    </xsl:template>

    <xsl:template match="Protocol">
        <xsl:text xml:space="preserve">&#10;&#10;</xsl:text>
        <xsl:comment> ################## Protocol <xsl:value-of select="../@name"/> ################ </xsl:comment>
        <xsl:text xml:space="preserve">&#10;</xsl:text>
        <xsl:comment xml:space="preserve"> IRP: <xsl:value-of select="../Irp"/> </xsl:comment>
        <xsl:text xml:space="preserve">&#10;</xsl:text>
        <axsl:template>
            <xsl:attribute name="match">girr:commandSet[girr:command/girr:parameters/@protocol = '<xsl:value-of select="lower-case(../@name)"/>']</xsl:attribute>
            <xsl:text xml:space="preserve">&#10;</xsl:text>
            <xsl:apply-templates select="BitspecIrstream" mode="warnIntroAndRepeat"/>
            <xsl:apply-templates select="BitspecIrstream" mode="warnEnding"/>
            <axsl:text xml:space="preserve">begin remote&#10;&#9;# Protocol name: <xsl:value-of select="../@name"/>&#10;&#9;name&#9;&#9;</axsl:text>
            <axsl:value-of select="../@name"/>
<axsl:text>
<xsl:apply-templates select="BitspecIrstream" mode="numberOfBits"/>
&#9;flags&#9;&#9;<xsl:apply-templates select="@standardPwm"/><xsl:apply-templates select="@biphase"/>
            <xsl:apply-templates select="BitspecIrstream/*[FiniteBitField][1]/Extent" mode="flags"/>
&#9;eps&#9;&#9;<xsl:value-of select="$eps"/>
&#9;aeps&#9;&#9;<xsl:value-of select="$aeps"/>
&#9;zero&#9;<xsl:apply-templates select="BitspecIrstream/BitSpec/BareIrStream[1]/*"/>
&#9;one&#9;<xsl:apply-templates select="BitspecIrstream/BitSpec/BareIrStream[2]/*"/>
<xsl:apply-templates select="BitspecIrstream" mode="header"/>
<xsl:apply-templates select="BitspecIrstream" mode="plead"/>
<xsl:apply-templates select="BitspecIrstream" mode="ptrail"/>
<xsl:apply-templates select="BitspecIrstream[Intro/*]/Repeat" mode="repeatFlag"/>
<xsl:apply-templates select="BitspecIrstream" mode="gapFlag"/>
<xsl:apply-templates select="BitspecIrstream[../@toggle='true' and */FiniteBitField/Data[.='T']]" mode="toggle_bit"/> <!-- obsolete synonom: repeat_bit -->
&#9;frequency&#9;<xsl:value-of select="GeneralSpec/@frequency"/>
&#9;begin codes
</axsl:text>
        <axsl:apply-templates select="//girr:command"/>
        <axsl:text>&#9;end codes
end remote
</axsl:text>
        </axsl:template>

        <axsl:template>
            <xsl:attribute name="name">command-<xsl:value-of select="harctoolbox:canonical-name(../@name)"/></xsl:attribute>
            <xsl:apply-templates select="ParameterSpecs/ParameterSpec"/>
            <axsl:text xml:space="preserve">&#9;&#9;</axsl:text>
            <axsl:value-of select="@name"/>
            <axsl:text>&#9;0x</axsl:text>
            <axsl:value-of>
                <xsl:attribute name="select">
                    <xsl:text>exporterutils:processBitFields(</xsl:text>
                    <xsl:apply-templates select="BitspecIrstream/*/FiniteBitField" mode="inCode"/>
                    <xsl:text>)</xsl:text>
                </xsl:attribute>
            </axsl:value-of>
            <axsl:text xml:space="preserve">
</axsl:text>
        </axsl:template>

        <xsl:apply-templates select="BitspecIrstream/*[FiniteBitField and ../../ParameterSpecs/ParameterSpec/Default]" mode="withDefaults"/>
        <xsl:apply-templates select="BitspecIrstream/Intro[FiniteBitField] | BitspecIrstream/Repeat[FiniteBitField]" mode="withoutDefaults"/>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="warnEnding"/>

    <xsl:template match="BitspecIrstream[Ending[*]]" mode="warnEnding">
        <axsl:text># Warning: Protocol contains ending that cannot be expressed in Lirc&#10;</axsl:text>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="warnIntroAndRepeat"/>

    <xsl:template match="BitspecIrstream[Intro/FiniteBitField and Repeat/FiniteBitField]" mode="warnIntroAndRepeat">
        <axsl:text># Warning: Protocol contains repeat elements that cannot be expressed in Lirc&#10;</axsl:text>
    </xsl:template>

    <xsl:template match="Intro|Repeat" mode="withoutDefaults">
        <xsl:comment> Version without defaults </xsl:comment>
        <axsl:template>
            <xsl:attribute name="match" xml:space="skip">
                <xsl:text>girr:command[girr:parameters/@protocol='</xsl:text>
                <xsl:value-of select="lower-case(../../../@name)"/>
                <xsl:text>'</xsl:text>
            <xsl:apply-templates select="../../ParameterSpecs/ParameterSpec[Default]" mode="default-path"/>]</xsl:attribute>
            <axsl:call-template>
                <xsl:attribute xml:space="skip" name="name">
                    <xsl:text>command-</xsl:text>
                    <xsl:value-of xml:space="skip" select="harctoolbox:canonical-name(../../../@name)"/>
                </xsl:attribute>
                <xsl:apply-templates select="../../ParameterSpecs/ParameterSpec" mode="inCodeWithoutDefaults"/>
            </axsl:call-template>
        </axsl:template>
    </xsl:template>

    <xsl:template match="Intro|Repeat" mode="withDefaults">
        <xsl:comment> Version with defaults </xsl:comment>
        <axsl:template>
            <xsl:attribute name="match">girr:command[girr:parameters/@protocol='<xsl:value-of select="lower-case(../../../@name)"/>']</xsl:attribute>
            <axsl:call-template>
                <xsl:attribute xml:space="skip" name="name">
                    <xsl:text>command-</xsl:text>
                    <xsl:value-of xml:space="skip" select="harctoolbox:canonical-name(../../../@name)"/>
                </xsl:attribute>
                <xsl:apply-templates select="../../ParameterSpecs/ParameterSpec" mode="inCodeWithDefaults"/>
            </axsl:call-template>
        </axsl:template>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="default-path" xml:space="skip">
        <xsl:text> and girr:parameters/girr:parameter[@name='</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>']</xsl:text>
    </xsl:template>

    <xsl:template match="ParameterSpec">
        <axsl:param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
        </axsl:param>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="inCodeWithoutDefaults">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>']/@value)</xsl:text>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="ParameterSpec" mode="inCodeWithDefaults">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>']/@value)</xsl:text>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="ParameterSpec[Default]" mode="inCodeWithDefaults">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:apply-templates select="Default"/>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="parameter" mode="inCode">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="select">
                <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
                <xsl:value-of select="@name"/>
                <xsl:text>']/@value)</xsl:text>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="Default">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Expression">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="BinaryOperator">
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="Expression[1]"/>
        <xsl:text>)</xsl:text>
        <xsl:value-of select="@kind"/>
        <xsl:text>(</xsl:text>
        <xsl:apply-templates select="Expression[2]"/>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="Name">
        <xsl:text>number(girr:parameters/girr:parameter[@name='</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>']/@value)</xsl:text>
    </xsl:template>

    <xsl:template name="bool-attribute">
        <xsl:value-of select="."/>
        <xsl:text>()</xsl:text>
    </xsl:template>

    <xsl:template match="@complement">
        <xsl:call-template name="bool-attribute"/>
    </xsl:template>

    <xsl:template match="@reverse">
        <xsl:call-template name="bool-attribute"/>
    </xsl:template>

    <xsl:template match="@reverse[ancestor::Protocol[GeneralSpec/@bitDirection='lsb']]">
        <xsl:if test=".='true'">
            <xsl:text>false()</xsl:text>
        </xsl:if>
        <xsl:if test="not(.='true')">
            <xsl:text>true()</xsl:text>
        </xsl:if>

    </xsl:template>

    <xsl:template match="finiteBitField" mode="namedTemplate">
        <axsl:with-param>
            <xsl:attribute name="name">
                <xsl:value-of select="data"/>
            </xsl:attribute>
        </axsl:with-param>
    </xsl:template>

    <xsl:template match="FiniteBitField" mode="inCode">
        <xsl:apply-templates select="@complement"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="@reverse"/>
        <xsl:text>, </xsl:text>

        <!--xsl:text>, number(girr:parameters/girr:parameter[@name='</xsl:text-->
        <xsl:apply-templates select="Data" mode="inFiniteBitField"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Width"/>
        <xsl:text>, </xsl:text>
        <xsl:apply-templates select="Chop"/>
        <xsl:if test="not(position()=last())">
            <xsl:text>, </xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Width|Chop">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="data" mode="inFiniteBitField">
        <xsl:apply-templates select="node()" mode="inFiniteBitField"/>
    </xsl:template>

    <xsl:template match="Name" mode="inFiniteBitField">
        <xsl-text>$</xsl-text>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="function" mode="toggle_bit"/>

    <xsl:template match="BitspecIrstream" mode="numberOfBits">
        <xsl:text xml:space="preserve">&#10;&#9;bits&#9;&#9;</xsl:text>
        <xsl:value-of select="*[FiniteBitField][1]/@numberOfBits"/>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="toggle_bit">
        <xsl:text xml:space="preserve">
&#9;toggle_bit&#9;</xsl:text>
        <xsl:apply-templates select="*/FiniteBitField[Data/Name[.='T']]" mode="toggle_bit_position"/>
    </xsl:template>

    <xsl:template match="FiniteBitField" mode="toggle_bit_position">
        <xsl:value-of select="1 + sum(preceding-sibling::FiniteBitField/Width)"/>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="header">
        <xsl:apply-templates select="Intro[*]" mode="header"/>
        <xsl:if test="not(Intro[*])">
            <xsl:apply-templates select="Repeat" mode="header"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Intro|Repeat" mode="header"/>

    <xsl:template match="Intro[ *[1][name()='Flash']  and  *[2][name()='Gap'] ]
                      | Repeat[ *[1][name()='Flash']  and  *[2][name()='Gap'] ]" mode="header">
        <xsl:text xml:space="preserve">
&#9;header&#9;</xsl:text>
        <xsl:apply-templates select="Flash[1]"/>
        <xsl:apply-templates select="Gap[1]"/>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="plead">
        <xsl:apply-templates select="Intro" mode="plead"/>
        <xsl:if test="not(Intro[*])">
            <xsl:apply-templates select="Repeat" mode="plead"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Intro|Repeat" mode="plead"/>

    <xsl:template match="Intro[*[1][name()='Flash']  and  *[2][name()='FiniteBitField']]
                      | Repeat[*[1][name()='Flash']  and  *[2][name()='FiniteBitField']]" mode="plead">
        <xsl:text xml:space="preserve">
&#9;plead&#9;</xsl:text>
        <xsl:apply-templates select="Flash[1]"/>
    </xsl:template>

    <xsl:template match="Intro[*[1][name()='Flash']  and  *[2][name()='Gap'] and *[3][name()='Flash'] ]
                      | Repeat[*[1][name()='Flash']  and  *[2][name()='Gap'] and *[3][name()='Flash'] ]" mode="plead">
        <xsl:text xml:space="preserve">
&#9;plead&#9;</xsl:text>
        <xsl:apply-templates select="Flash[2]"/>
    </xsl:template>

    <xsl:template match="Repeat" mode="repeatFlag"/>

    <xsl:template match="Repeat[*[1][name()='Flash']  and  *[2][name()='Gap']]" mode="repeatFlag">
        <xsl:text xml:space="preserve">
&#9;repeat&#9;</xsl:text>
        <xsl:apply-templates select="Flash[1]"/>
        <xsl:apply-templates select="Gap[1]"/>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="gapFlag">
        <xsl:apply-templates select="Intro" mode="gapFlag"/>
        <xsl:if test="not(Intro[*])">
            <xsl:apply-templates select="Repeat" mode="gapFlag"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Intro|Repeat" mode="gapFlag"/>

    <xsl:template match="Intro[Extent]|Repeat[Extent]" mode="gapFlag">
        <xsl:text xml:space="preserve">
&#9;gap&#9;</xsl:text>
        <xsl:apply-templates select="Extent" mode="gap"/>
    </xsl:template>

    <xsl:template match="Intro[*[position()=last()][name()='Gap']]
                      | Repeat[*[position()=last()][name()='Gap']]" mode="gapFlag">
        <xsl:text xml:space="preserve">
&#9;gap&#9;</xsl:text>
        <xsl:apply-templates select="Gap[position()=last()]"/>
    </xsl:template>

    <xsl:template match="BitspecIrstream" mode="ptrail">
        <xsl:apply-templates select="Intro" mode="ptrail"/>
        <xsl:if test="not(Intro[*])">
            <xsl:apply-templates select="Repeat" mode="ptrail"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Intro|Repeat" mode="ptrail"/>

    <xsl:template match="Intro[Flash[preceding-sibling::FiniteBitField]]
                      | Repeat[Flash[preceding-sibling::FiniteBitField]]" mode="ptrail">
        <xsl:text xml:space="preserve">
&#9;ptrail&#9;</xsl:text>
        <xsl:apply-templates select="Flash[preceding-sibling::FiniteBitField and position()=last()]"/>
    </xsl:template>

    <xsl:template xml:space="skip" name="multiply">
        <xsl:param name="x"/>
        <xsl:param name="y"/>
        <xsl:value-of select="round(number($x)*number($y))"/>
    </xsl:template>

    <xsl:template match="Flash[@unit='']|Gap[@unit='']">
        <xsl:text>&#9;</xsl:text>
        <xsl:call-template name="multiply">
            <xsl:with-param name="x">
                <xsl:apply-templates select="*"/>
            </xsl:with-param>
            <xsl:with-param name="y">
                <xsl:value-of select="number(ancestor::Protocol/GeneralSpec/@unit)"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="Flash[@unit='m']|Gap[@unit='m']">
        <xsl:text>&#9;</xsl:text>
        <xsl:call-template name="multiply">
            <xsl:with-param name="x">
                <xsl:apply-templates select="*"/>
            </xsl:with-param>
            <xsl:with-param name="y" select="1000"/>
        </xsl:call-template>
        <xsl:value-of select="1000 * @time"/>
    </xsl:template>

    <xsl:template match="Extent[@unit='m']" mode="gap">
        <xsl:text>&#9;</xsl:text>
        <xsl:call-template name="multiply">
            <xsl:with-param name="x">
                <xsl:apply-templates select="*"/>
            </xsl:with-param>
            <xsl:with-param name="y" select="1000"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="Number">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="NameOrNumber">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="NumberWithDecimals">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="@standardPwm[.='true']">
        <xsl:text>SPACE_ENC</xsl:text>
    </xsl:template>

    <xsl:template match="@biphase[.='true']">
        <xsl:text>RC5</xsl:text>
    </xsl:template>

    <!-- REVERSE is not reliable; do not use -->

    <xsl:template match="Extent" mode="flags">
        <xsl:text>|CONST_LENGTH</xsl:text>
    </xsl:template>

</xsl:transform>