package in.hangang.service;


import in.hangang.domain.*;
import in.hangang.domain.scrap.ScrapLectureBank;
import in.hangang.response.BaseResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface LectureBankService {
    //Main====================================================================================
    List<LectureBank> searchLectureBanks(LectureBankCriteria lectureBankCriteria) throws Exception;
    LectureBank getLectureBank(Long id) throws Exception;
    BaseResponse postLectureBank(LectureBank lectureBank) throws Exception;
    BaseResponse updateLectureBank(LectureBank lectureBank, Long id) throws Exception;
    void deleteLectureBank(Long id) throws Exception;


    //comment====================================================================================
    List<LectureBankComment> getComments(Long lecture_bank_id);
    BaseResponse addComment(Long lecture_bank_id, String comments) throws Exception;
    BaseResponse setComment(Long lecture_bank_comment_id, Long commentId,String comments) throws Exception;
    BaseResponse deleteComment(Long lecture_bank_comment_id,Long commentId) throws Exception;

    //purchase====================================================================================
    Boolean checkPurchase(Long lecture_bank_id) throws Exception;
    void purchase(Long lecture_bank_id) throws Exception;


    //hits====================================================================================
    void pushHit(Long lecture_bank_id) throws Exception;

    //file====================================================================================

    //UPLOAD
    String fileUpload(MultipartFile file) throws Exception;
    String getObjectUrl(Long id) throws Exception;

    //Thumbnail====================================================================================

    //Scrap====================================================================================
    void createScrap(Long lecture_bank_id) throws Exception;
    void deleteScrap(ArrayList<Long> lectureBank_IDList) throws Exception;
    List<ScrapLectureBank> getScrapList() throws Exception;

}
