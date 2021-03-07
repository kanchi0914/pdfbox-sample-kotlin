import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File


object PdfBoxSample {

    fun main() {
    }

    fun loadPdf(filePath: String): PDDocument {
        return PDDocument.load(File(filePath))
    }

    /**
     * テキストを追加
     */
    fun addText(doc: PDDocument, page: PDPage, text: String) {
        val stream = PDPageContentStream(doc, page)
        stream.beginText();
        stream.setFont(PDType1Font.HELVETICA, 20f);
        stream.moveTextPositionByAmount(100f, 100f);
        stream.drawString(text);
        stream.endText();
        stream.close();
    }

    /**
     * 複数行のテキストを追加
     */
    fun addTexts(doc: PDDocument, page: PDPage, texts: List<String>) {
        val stream = PDPageContentStream(doc, page)
        stream.setFont(PDType1Font.HELVETICA, 20f)
        stream.beginText()
        stream.setLeading(20.0)
        stream.newLineAtOffset(100f, 100f)
        texts.forEach() {
            stream.showText(it)
            stream.newLine()
        }
        stream.endText()
        stream.close();
    }

    /**
     * 読み込んだ複数のpdfファイルを結合
     */
    fun mergePdfFiles(files: List<File>, outPutFilePath: String) {
        val merger = PDFMergerUtility()
        merger.destinationFileName = outPutFilePath
        files.forEach() {
            merger.addSource(it)
        }
        merger.mergeDocuments()
    }

    /**
     * 複数のPDDocumentを結合
     */
    fun mergePDDocuments(docs: List<PDDocument>): PDDocument {
        val merged = PDDocument()
        docs.forEach { doc ->
            doc.pages.forEach { page -> merged.addPage(page) }
        }
        return merged
    }

    /**
     * PDDocumentを指定されたページ以下から分割して保存する
     */
    fun splitIntoTwo(doc: PDDocument, pageNumber: Int) {
        val splitter = Splitter()
        val list = splitter.split(doc)
        val docs1 = list.subList(0, pageNumber - 1)
        val docs2 = list.subList(pageNumber, list.lastIndex)
        val doc1 = mergePDDocuments(docs1)
        val doc2 = mergePDDocuments(docs2)
        doc1.save("split_0.pdf")
        doc2.save("split_1.pdf")
        doc1.close()
        doc2.close()
    }

    /**
     * PDDocumentをページごとに分割
     */
    fun split(doc: PDDocument): List<PDDocument> {
        val splitter = Splitter()
        return splitter.split(doc)
    }

    /**
     * PDFを暗号化
     */
    fun encryptPdf(doc: PDDocument, ownerPass: String, userPass: String) {
        val accessPermission = AccessPermission()
        val spp = StandardProtectionPolicy(ownerPass, userPass, accessPermission)
        spp.setEncryptionKeyLength(128);
        spp.setPermissions(accessPermission);
        // そのままだとエラーが出る
        // https://github.com/sir-ragna/noprint/issues/1
        doc.protect(spp);
    }

    /**
     * 指定ディレクトリ以下の画像をまとめて一つのPDFにして保存
     */
    fun createPdfFromImages(resourceDirPath: String, outPutFilePath: String) {
        val doc = PDDocument()
        val resourceDir = File(resourceDirPath)
        resourceDir.listFiles { f -> f.isFile }.forEach {
            println("ページ追加:$it.path")
            var pdImage = PDImageXObject.createFromFile(it.path, doc)
            var page = PDPage(PDRectangle(pdImage.image.width.toFloat(), pdImage.image.height.toFloat()))
            doc.addPage(page)
            var stream = PDPageContentStream(doc, page)
            stream.drawImage(pdImage, 0f, 0f)
            stream.close()
        }
        doc.save(outPutFilePath)
        doc.close()
    }
}