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
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import javax.imageio.ImageIO


object PdfBoxSample {

    fun main() {
        do2()
    }

    fun loadPdf(filePath: String): PDDocument {
        return PDDocument.load(File(filePath))
    }

    fun createWithImageMagick(){
        var list = mutableListOf("convert", "*.jpg", "out9999.pdf")
        val runtime = Runtime.getRuntime()
        val str = list.joinToString(" ")
        try{
            runtime.exec(str)
        }catch (e:Exception){
            println(1)
        }

        var p: Process? = null
        val dir = File("C:\\Users\\user\\Desktop\\Korolevna\\") // 実行ディレクトリの指定

        try {
            p = runtime.exec(str, null, dir) // 実行ディレクトリ(dir)でCommand(mecab.exe)を実行する
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            p!!.waitFor() // プロセスの正常終了まで待機させる
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val `is` = p!!.inputStream // プロセスの結果を変数に格納する

        val br = BufferedReader(InputStreamReader(`is`)) // テキスト読み込みを行えるようにする


        while (true) {
            val line = br.readLine()
            if (line == null) {
                break // 全ての行を読み切ったら抜ける
            } else {
                println("line : $line") // 実行結果を表示
            }
        }

    }


    fun do2(){
        val pb =  ProcessBuilder("cmd.exe", "/C", "start", "convert")
        pb.redirectErrorStream(true)
        val env = pb.environment()
        val a = env.get("Path")
//        env.put("Path", "C:\\ImageMagick-7.0.11-2-portable-Q16-x64; " + a)
        val p = pb.start()
        val br = BufferedReader(InputStreamReader(p.inputStream, "MS932"))
        var line: String? = null
        while (br.readLine().also { line = it } != null) {
            println(line)
        }
        println(p.waitFor())
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
//            val image = loadImage(it.path);
//            val pdImage = LosslessFactory.createFromImage(doc, image)
            val pdImage = PDImageXObject.createFromFileByExtension(File(it.path), doc)
            var page = PDPage(PDRectangle(pdImage.image.width.toFloat(), pdImage.image.height.toFloat()))
            doc.addPage(page)
            var stream = PDPageContentStream(doc, page)
            stream.drawImage(pdImage, 0f, 0f)
            stream.close()
        }
        doc.save(outPutFilePath)
        doc.close()
    }

    fun createPdfFromImagesFromParentDir(resourceDirPath: String){
        val resourceDir = File(resourceDirPath)
        resourceDir.listFiles { f -> f.isDirectory }.forEach {
            createPdfFromImages(it.path, "output/${it.name}_3.pdf")
        }
    }

    fun loadImage(path:String): BufferedImage {
        var image = ImageIO.read(File(path))
        return image
    }

}