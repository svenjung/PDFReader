package com.svenj.tools.pdf.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface PdfDAO {

    @Query("SELECT * FROM Pdf ORDER BY read_time DESC")
    Flowable<List<Pdf>> getAllPdfs();

    @Query("SELECT * FROM Pdf WHERE read_time != 0 ORDER BY read_time DESC")
    LiveData<List<Pdf>> getRecentPdfs();

    @Query("SELECT * FROM Pdf WHERE file_path = :path LIMIT 1")
    Single<Pdf> getPdf(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPdf(Pdf pdf);

    //@Update

    @Delete
    int deletePdf(Pdf pdf);
}
