package com.svenj.tools.pdf.repositories;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "pdf")
public class Pdf {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "file_name")
    private String fileName;

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "file_size")
    private long fileSize;

    @ColumnInfo(name = "create_time")
    private Date createTime;

    @ColumnInfo(name = "read_page")
    private int readPage;

    @ColumnInfo(name = "read_time")
    private Date readTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getReadPage() {
        return readPage;
    }

    public void setReadPage(int readPage) {
        this.readPage = readPage;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Ignore
    public Pdf(String fileName, String filePath, long fileSize, Date createTime) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.createTime = createTime;
    }

    public Pdf() {

    }

    @Override
    public String toString() {
        return "Pdf{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", createTime=" + createTime +
                ", readPage=" + readPage +
                ", readTime=" + readTime +
                '}';
    }
}
