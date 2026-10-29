import os
import re
import html
import zipfile
import xml.sax.saxutils as saxutils

def escape_xml(text):
    return saxutils.escape(text)

def generate_doc_html(md_path, doc_path):
    with open(md_path, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    html_lines = []
    
    html_lines.append("""<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
<head>
<meta charset="utf-8">
<title>Motor Insurance Claim Portal - DevOps Capstone Project Report</title>
<!--[if gte mso 9]>
<xml>
<w:WordDocument>
<w:View>Print</w:View>
<w:Zoom>100</w:Zoom>
<w:DoNotOptimizeForBrowser/>
</w:WordDocument>
</xml>
<![endif]-->
<style>
@page Section1 {
    size: 8.5in 11.0in;
    margin: 1.0in 1.0in 1.0in 1.0in;
    mso-header-margin: 0.5in;
    mso-footer-margin: 0.5in;
    mso-paper-source: 0;
}
div.Section1 { page: Section1; }
body {
    font-family: 'Calibri', 'Segoe UI', Arial, sans-serif;
    font-size: 11pt;
    line-height: 1.5;
    color: #222222;
}
h1 {
    font-family: 'Calibri Light', 'Segoe UI', Arial, sans-serif;
    font-size: 20pt;
    color: #003366;
    border-bottom: 2px solid #003366;
    padding-bottom: 4px;
    margin-top: 24pt;
    margin-bottom: 8pt;
    page-break-after: avoid;
}
h2 {
    font-family: 'Calibri Light', 'Segoe UI', Arial, sans-serif;
    font-size: 15pt;
    color: #004c99;
    border-bottom: 1px solid #cce0ff;
    padding-bottom: 3px;
    margin-top: 18pt;
    margin-bottom: 6pt;
    page-break-after: avoid;
}
h3 {
    font-family: 'Calibri', 'Segoe UI', Arial, sans-serif;
    font-size: 12pt;
    color: #003366;
    font-weight: bold;
    margin-top: 14pt;
    margin-bottom: 4pt;
    page-break-after: avoid;
}
h4 {
    font-family: 'Calibri', 'Segoe UI', Arial, sans-serif;
    font-size: 11pt;
    color: #004c99;
    font-weight: bold;
    margin-top: 10pt;
    margin-bottom: 3pt;
    page-break-after: avoid;
}
p {
    margin-top: 0;
    margin-bottom: 8pt;
    text-align: justify;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin-top: 10pt;
    margin-bottom: 14pt;
    font-size: 10pt;
}
th {
    background-color: #003366;
    color: #ffffff;
    font-weight: bold;
    text-align: left;
    padding: 6pt 8pt;
    border: 1px solid #003366;
}
td {
    padding: 5pt 8pt;
    border: 1px solid #cccccc;
    vertical-align: top;
}
tr:nth-child(even) td {
    background-color: #f7fafc;
}
pre {
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 9pt;
    background-color: #f4f6f8;
    border: 1px solid #d0d7de;
    border-left: 4px solid #003366;
    padding: 8pt 10pt;
    margin: 8pt 0 10pt 0;
    white-space: pre-wrap;
    word-break: break-all;
}
code {
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 9.5pt;
    background-color: #edf2f7;
    padding: 1px 4px;
    border-radius: 3px;
    color: #003366;
}
ul, ol {
    margin-top: 0;
    margin-bottom: 8pt;
    padding-left: 20pt;
}
li {
    margin-bottom: 3pt;
}
blockquote {
    border-left: 3px solid #003366;
    margin: 6pt 0 6pt 10pt;
    padding: 4pt 8pt;
    background: #f0f4f8;
}
hr {
    border: 0;
    height: 1px;
    background: #cce0ff;
    margin: 16pt 0;
}
</style>
</head>
<body>
<div class="Section1">
""")

    in_code_block = False
    code_lines = []
    in_table = False
    table_lines = []

    for line in lines:
        stripped = line.strip()

        # Handle code blocks
        if stripped.startswith('```'):
            if in_code_block:
                in_code_block = False
                code_text = html.escape('\n'.join(code_lines))
                html_lines.append(f"<pre><code>{code_text}</code></pre>")
                code_lines = []
            else:
                in_code_block = True
                code_lines = []
            continue

        if in_code_block:
            code_lines.append(line)
            continue

        # Ignore any screenshot box lines if encountered
        if stripped.startswith('┌─') or stripped.startswith('+─') or stripped.startswith('└─'):
            continue
        if stripped.startswith('│') or (stripped.startswith('|') and 'PLACEHOLDER' in stripped):
            continue

        # Handle Tables
        if stripped.startswith('|') and stripped.endswith('|'):
            if not in_table:
                in_table = True
                table_lines = [stripped]
            else:
                table_lines.append(stripped)
            continue
        else:
            if in_table:
                in_table = False
                html_lines.append("<table>")
                header_done = False
                for t_idx, t_row in enumerate(table_lines):
                    if re.match(r'^\|[\s\-:]+(\|[\s\-:]+)+\|$', t_row):
                        header_done = True
                        continue
                    cols = [c.strip() for c in t_row.strip('|').split('|')]
                    html_lines.append("<tr>")
                    tag = "th" if not header_done else "td"
                    for col in cols:
                        col_html = html.escape(col)
                        col_html = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', col_html)
                        col_html = re.sub(r'`(.+?)`', r'<code>\1</code>', col_html)
                        html_lines.append(f"<{tag}>{col_html}</{tag}>")
                    html_lines.append("</tr>")
                html_lines.append("</table>")
                table_lines = []

        # Headings
        if stripped.startswith('# '):
            h_text = html.escape(stripped[2:])
            html_lines.append(f"<h1>{h_text}</h1>")
        elif stripped.startswith('## '):
            h_text = html.escape(stripped[3:])
            html_lines.append(f"<h2>{h_text}</h2>")
        elif stripped.startswith('### '):
            h_text = html.escape(stripped[4:])
            html_lines.append(f"<h3>{h_text}</h3>")
        elif stripped.startswith('#### '):
            h_text = html.escape(stripped[5:])
            html_lines.append(f"<h4>{h_text}</h4>")
        elif stripped == '---':
            html_lines.append("<hr/>")
        elif stripped.startswith('- ') or stripped.startswith('* '):
            l_text = html.escape(stripped[2:])
            l_text = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', l_text)
            l_text = re.sub(r'`(.+?)`', r'<code>\1</code>', l_text)
            html_lines.append(f"<ul><li>{l_text}</li></ul>")
        elif re.match(r'^[0-9]+\.\s+', stripped):
            m = re.match(r'^[0-9]+\.\s+(.+)$', stripped)
            l_text = html.escape(m.group(1))
            l_text = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', l_text)
            l_text = re.sub(r'`(.+?)`', r'<code>\1</code>', l_text)
            html_lines.append(f"<ol><li>{l_text}</li></ol>")
        elif stripped.startswith('> '):
            q_text = html.escape(stripped[2:])
            q_text = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', q_text)
            q_text = re.sub(r'`(.+?)`', r'<code>\1</code>', q_text)
            html_lines.append(f"<blockquote>{q_text}</blockquote>")
        elif stripped:
            p_text = html.escape(stripped)
            p_text = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', p_text)
            p_text = re.sub(r'`(.+?)`', r'<code>\1</code>', p_text)
            html_lines.append(f"<p>{p_text}</p>")

    html_lines.append("""
</div>
</body>
</html>
""")

    with open(doc_path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(html_lines))
    print(f"[OK] Generated Word .doc file at: {doc_path} ({os.path.getsize(doc_path)} bytes)")


def generate_docx_package(md_path, docx_path):
    with open(md_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    body_xml = []
    
    in_code = False
    code_lines = []
    in_table = False
    table_lines = []

    def make_run(text, bold=False, italic=False, font="Calibri", size=22, color=None):
        rPr = []
        if font:
            rPr.append(f'<w:rFonts w:ascii="{font}" w:hAnsi="{font}"/>')
        if bold:
            rPr.append('<w:b/>')
        if italic:
            rPr.append('<w:i/>')
        if color:
            rPr.append(f'<w:color w:val="{color}"/>')
        if size:
            rPr.append(f'<w:sz w:val="{size}"/>')
        rPr_str = f'<w:rPr>{"".join(rPr)}</w:rPr>' if rPr else ''
        t_str = f'<w:t xml:space="preserve">{escape_xml(text)}</w:t>'
        return f'<w:r>{rPr_str}{t_str}</w:r>'

    def make_paragraph(runs, style=None, before=0, after=120, line_spacing=276, border=None, fill=None):
        pPr = []
        if style:
            pPr.append(f'<w:pStyle w:val="{style}"/>')
        if before or after:
            pPr.append(f'<w:spacing w:before="{before}" w:after="{after}" w:line="{line_spacing}" w:lineRule="auto"/>')
        if fill:
            pPr.append(f'<w:shd w:val="clear" w:color="auto" w:fill="{fill}"/>')
        if border:
            pPr.append(f'<w:pBdr><w:top w:val="single" w:sz="8" w:space="4" w:color="{border}"/><w:left w:val="single" w:sz="8" w:space="4" w:color="{border}"/><w:bottom w:val="single" w:sz="8" w:space="4" w:color="{border}"/><w:right w:val="single" w:sz="8" w:space="4" w:color="{border}"/></w:pBdr>')
        pPr_str = f'<w:pPr>{"".join(pPr)}</w:pPr>' if pPr else ''
        return f'<w:p>{pPr_str}{"".join(runs)}</w:p>'

    for line in lines:
        stripped = line.strip()

        if stripped.startswith('```'):
            if in_code:
                in_code = False
                for cl in code_lines:
                    r = make_run(cl, font="Consolas", size=18, color="112233")
                    body_xml.append(make_paragraph([r], before=0, after=40, line_spacing=240, fill="F4F6F8"))
                code_lines = []
            else:
                in_code = True
                code_lines = []
            continue

        if in_code:
            code_lines.append(line.rstrip('\r\n'))
            continue

        # Ignore any screenshot box lines if encountered
        if stripped.startswith('┌─') or stripped.startswith('+─') or stripped.startswith('└─'):
            continue
        if stripped.startswith('│') or (stripped.startswith('|') and 'PLACEHOLDER' in stripped):
            continue

        # Handle Tables
        if stripped.startswith('|') and stripped.endswith('|'):
            if not in_table:
                in_table = True
                table_lines = [stripped]
            else:
                table_lines.append(stripped)
            continue
        else:
            if in_table:
                in_table = False
                tbl_rows = []
                header_done = False
                for t_row in table_lines:
                    if re.match(r'^\|[\s\-:]+(\|[\s\-:]+)+\|$', t_row):
                        header_done = True
                        continue
                    cols = [c.strip() for c in t_row.strip('|').split('|')]
                    row_cells = []
                    for c_val in cols:
                        fill_color = "003366" if not header_done else "FFFFFF"
                        text_color = "FFFFFF" if not header_done else "222222"
                        bold_flag = True if not header_done else False
                        
                        cell_p = make_paragraph([make_run(c_val, bold=bold_flag, size=18, color=text_color)], before=60, after=60)
                        tc = f'''<w:tc>
                            <w:tcPr>
                                <w:shd w:val="clear" w:color="auto" w:fill="{fill_color}"/>
                                <w:tcMar>
                                    <w:top w:w="80" w:type="dxa"/>
                                    <w:left w:w="120" w:type="dxa"/>
                                    <w:bottom w:w="80" w:type="dxa"/>
                                    <w:right w:w="120" w:type="dxa"/>
                                </w:tcMar>
                            </w:tcPr>
                            {cell_p}
                        </w:tc>'''
                        row_cells.append(tc)
                    tbl_rows.append(f'<w:tr>{"".join(row_cells)}</w:tr>')
                
                table_xml = f'''<w:tbl>
                    <w:tblPr>
                        <w:tblW w:w="0" w:type="auto"/>
                        <w:tblBorders>
                            <w:top w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
                            <w:left w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
                            <w:bottom w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
                            <w:right w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
                            <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E0E0E0"/>
                            <w:insideV w:val="single" w:sz="4" w:space="0" w:color="E0E0E0"/>
                        </w:tblBorders>
                    </w:tblPr>
                    {"".join(tbl_rows)}
                </w:tbl>'''
                body_xml.append(table_xml)
                table_lines = []

        # Headings
        if stripped.startswith('# '):
            r = make_run(stripped[2:], bold=True, font="Calibri Light", size=38, color="003366")
            body_xml.append(make_paragraph([r], style="Heading1", before=360, after=120))
        elif stripped.startswith('## '):
            r = make_run(stripped[3:], bold=True, font="Calibri Light", size=28, color="004C99")
            body_xml.append(make_paragraph([r], style="Heading2", before=240, after=100))
        elif stripped.startswith('### '):
            r = make_run(stripped[4:], bold=True, font="Calibri", size=24, color="003366")
            body_xml.append(make_paragraph([r], style="Heading3", before=180, after=60))
        elif stripped == '---':
            r = make_run("_____________________________________________________________________________", color="CCE0FF")
            body_xml.append(make_paragraph([r], before=80, after=80))
        elif stripped.startswith('- ') or stripped.startswith('* '):
            r_bullet = make_run("•  ", bold=True, color="003366")
            r_text = make_run(stripped[2:], size=22)
            body_xml.append(make_paragraph([r_bullet, r_text], before=20, after=40))
        elif re.match(r'^[0-9]+\.\s+', stripped):
            m = re.match(r'^([0-9]+\.)\s+(.+)$', stripped)
            r_num = make_run(m.group(1) + " ", bold=True, color="003366")
            r_text = make_run(m.group(2), size=22)
            body_xml.append(make_paragraph([r_num, r_text], before=20, after=40))
        elif stripped:
            r = make_run(stripped, size=22)
            body_xml.append(make_paragraph([r], before=0, after=100))

    body_xml.append('''<w:sectPr>
        <w:pgSz w:w="12240" w:h="15840"/>
        <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>''')

    document_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:body>
        {"".join(body_xml)}
    </w:body>
</w:document>'''

    content_types_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
    <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>'''

    rels_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>'''

    word_rels_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>'''

    styles_xml = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:docDefaults>
        <w:rPrDefault>
            <w:rPr>
                <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                <w:sz w:val="22"/>
                <w:szCs w:val="22"/>
                <w:lang w:val="en-US"/>
            </w:rPr>
        </w:rPrDefault>
        <w:pPrDefault>
            <w:pPr>
                <w:spacing w:line="276" w:lineRule="auto" w:after="120"/>
            </w:pPr>
        </w:pPrDefault>
    </w:docDefaults>
    <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
        <w:name w:val="Normal"/>
    </w:style>
    <w:style w:type="paragraph" w:styleId="Heading1">
        <w:name w:val="heading 1"/>
        <w:pPr>
            <w:keepNext/>
            <w:spacing w:before="360" w:after="120"/>
        </w:pPr>
        <w:rPr>
            <w:rFonts w:ascii="Calibri Light" w:hAnsi="Calibri Light"/>
            <w:b/>
            <w:color w:val="003366"/>
            <w:sz w:val="36"/>
        </w:rPr>
    </w:style>
    <w:style w:type="paragraph" w:styleId="Heading2">
        <w:name w:val="heading 2"/>
        <w:pPr>
            <w:keepNext/>
            <w:spacing w:before="240" w:after="100"/>
        </w:pPr>
        <w:rPr>
            <w:rFonts w:ascii="Calibri Light" w:hAnsi="Calibri Light"/>
            <w:b/>
            <w:color w:val="004C99"/>
            <w:sz w:val="28"/>
        </w:rPr>
    </w:style>
    <w:style w:type="paragraph" w:styleId="Heading3">
        <w:name w:val="heading 3"/>
        <w:pPr>
            <w:keepNext/>
            <w:spacing w:before="160" w:after="60"/>
        </w:pPr>
        <w:rPr>
            <w:b/>
            <w:color w:val="003366"/>
            <w:sz w:val="24"/>
        </w:rPr>
    </w:style>
</w:styles>'''

    with zipfile.ZipFile(docx_path, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', content_types_xml)
        docx.writestr('_rels/.rels', rels_xml)
        docx.writestr('word/_rels/document.xml.rels', word_rels_xml)
        docx.writestr('word/styles.xml', styles_xml)
        docx.writestr('word/document.xml', document_xml)

    print(f"[OK] Generated Word .docx file at: {docx_path} ({os.path.getsize(docx_path)} bytes)")

if __name__ == '__main__':
    md_file = r'D:\DevOps\PROJECT_REPORT.md'
    doc_file = r'D:\DevOps\PROJECT_REPORT.doc'
    docx_file = r'D:\DevOps\PROJECT_REPORT.docx'
    
    generate_doc_html(md_file, doc_file)
    generate_docx_package(md_file, docx_file)
