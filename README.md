# PDFReader
一款简单的PDF阅读工具，基于pdfium实现

<img width="200" height="400" src="https://github.com/svenjung/PDFReader/blob/master/screenshots/home_recent.png"/> <img width="200" height="400" src="https://github.com/svenjung/PDFReader/blob/master/screenshots/reader_main.png"/> <img width="200" height="400" src="https://github.com/svenjung/PDFReader/blob/master/screenshots/bookmark_list.png"/>
## 功能
目前只有阅读功能，并保存阅读记录

## 目标
开发一款其他PDF软件需要收费的功能：合并，拆分等

## BUG
>* PdfView 中纵向浏览时，无法设置分割线，除了给PdfView设置背景。但这会引起另外一个问题，打开activity时会闪（先显示PdfView的背景）
   这也可以处理，给PdfView盖一层遮罩（明显的过度绘制啊）。
>* PdfView在缩放的效果不太友好

### Links:
- [android-pdf-view](https://github.com/barteksc/AndroidPdfViewer)
- [iTextPdf](https://github.com/itext/itextpdf)
- [RecyclerTreeView](https://github.com/TellH/RecyclerTreeView)
