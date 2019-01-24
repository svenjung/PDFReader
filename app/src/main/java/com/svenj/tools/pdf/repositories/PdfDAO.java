package com.svenj.tools.pdf.repositories;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * room结合RxJava目前看智能在查询上
 */
@Dao
public interface PdfDAO {

    @Query("SELECT * FROM Pdf WHERE read_time != 0 ORDER BY read_time DESC")
    Flowable<List<Pdf>> getRecentPdf();

    @Query("SELECT * FROM Pdf WHERE read_time != 0 ORDER BY read_time DESC")
    Flowable<List<Pdf>> getAllPdf();

    @Query("SELECT * FROM Pdf WHERE file_path = :path LIMIT 1")
    Single<Pdf> getPdf(String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPdf(Pdf pdf);

    //@Update

    @Delete
    int deletePdf(Pdf pdf);
}
