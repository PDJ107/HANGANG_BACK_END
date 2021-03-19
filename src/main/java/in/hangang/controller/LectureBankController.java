package in.hangang.controller;

import com.google.common.net.HttpHeaders;
import in.hangang.annotation.Auth;
import in.hangang.domain.*;
import in.hangang.service.LectureBankService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/lecture-banks")
public class LectureBankController {

    @Autowired
    private LectureBankService lectureBankService;


    // 강의자료 MAIN------------------------------------------------------------------------------------
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 목록 가져오기" , notes = "강의자료 목록을 전체, 필터별로 가져올 수 있습니다.")
    public @ResponseBody
    ResponseEntity getSearchLectureBanks(@ModelAttribute("criteria") LectureBankCriteria lectureBankCriteria) {
        return new ResponseEntity<List<LectureBank>>(lectureBankService.searchLectureBanks(lectureBankCriteria), HttpStatus.OK);
    }


    @RequestMapping(value = "/main/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 상세 페이지" , notes = "강의자료의  상세한 정보, hit를 눌렀는가와 구매여부에 관한 정보\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity<LectureBank> getLectureBank(@PathVariable Long id) throws Exception {
        return new ResponseEntity<LectureBank>(lectureBankService.getLectureBank(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/main/file/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 파일 목록" , notes = "강의자료록에 해당하는 파일의 목록만을 가져옵니다(파일이름, 확장자)\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity<List<UploadFile>> getFileList(@PathVariable Long id) throws Exception {
        return new ResponseEntity<List<UploadFile>>(lectureBankService.getFileList(id),HttpStatus.OK);
    }



    //강의자료 CUD------------------------------------------------------------------------------------
    /*
    write(GET) 작성시작시 lecturebank_id 반환 -> 파일 업로드 시 해당 파일과 함께 lecturebank_id 입력 -> /write POST로 강의자료 내용 전송
     */

    //OMG NO semester on LectureBank ()
    //**add semester to LectureBank*******************************************************
    @Auth
    @RequestMapping(value = "/write", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 작성하기" , notes = "작성하기를 누르면 lecturebank id가 생성되어 반환됩니다")
    public @ResponseBody
    ResponseEntity<Long> createLectureBank() throws Exception {
        return new ResponseEntity<Long>(lectureBankService.createLectureBank(), HttpStatus.CREATED);
    }

    @Auth
    @RequestMapping(value = "/write", method = RequestMethod.POST)
    @ApiOperation(value ="강의자료 작성완료" , notes = "id를 포함하여 작성한 강의 자료를 전송합니다")
    public @ResponseBody
    ResponseEntity submitLectureBank(@RequestBody LectureBank lectureBank) throws Exception {
        lectureBankService.submitLectureBank(lectureBank);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Auth
    @RequestMapping(value = "/modify", method = RequestMethod.PATCH)
    @ApiOperation(value ="강의자료 수정" , notes = "강의 자료를 수정합니다\n파리미터는 강의자료 id 입니다")
    public @ResponseBody
    ResponseEntity modifyLectureBank(@RequestBody LectureBank lectureBank) throws Exception {
        lectureBankService.setLectureBank(lectureBank);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Auth
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value ="강의자료 작성 취소 및 삭제" , notes = "강의 자료를 삭제합니다\n파리미터는 강의자료 id 입니다")
    public @ResponseBody
    ResponseEntity deleteLectureBank(@PathVariable Long id) throws Exception {
        lectureBankService.deleteLectureBank(id);
        //make all available =2 upload file *******************************************************************************
        return new ResponseEntity(HttpStatus.OK);
    }



    //File------------------------------------------------------------------------------------


    @Auth
    @RequestMapping(value = "/file/upload/{id}", method = RequestMethod.POST)
    @ApiOperation(value ="단일 파일 업로드" , notes = "파일을 1개 업로드 합니다.\n파라미터는 강의 자료 id 입니다.\n업로드된 파일의 id가 반환됩니다.")
    public @ResponseBody
    ResponseEntity<Long> uploadFile(@ApiParam(required = true) @RequestBody MultipartFile file, @PathVariable Long id) throws Exception {
        return new ResponseEntity<Long>(lectureBankService.fileUpload(file, id),HttpStatus.CREATED);
    }

    @Auth
    @ApiImplicitParams(
            @ApiImplicitParam(name = "files", required = true, dataType = "__file", paramType = "form")
    )
    @RequestMapping(value = "/files/upload/{id}", method = RequestMethod.POST)
    @ApiOperation(value ="다중 파일 업로드" , notes = "여러개의 파일을 업로드 합니다.\n파라미터는 강의 자료 id 입니다.\n업로드된 파일의 id 목록이 반환됩니다.")
    public ResponseEntity<List<Long>> uploadFiles(@ApiParam(name = "files") @RequestParam(value = "files",required = true) MultipartFile[] files, @PathVariable Long id) throws Exception {
        List<MultipartFile> list = new ArrayList<>();
        for(MultipartFile file : files){
            list.add(file);
        }
        return new ResponseEntity<List<Long>>(lectureBankService.LectureBankFilesUpload(list, id), HttpStatus.CREATED);
    }


    //delete the upload *******************************************************************************
    //by Scheduler


    //upload_file table - available FLAG 0:업로드 대기 /  1: 업로드 완료 /  2: 삭제
    @Auth
    @RequestMapping(value = "/file/cancel_upload/{id}", method = RequestMethod.POST)
    @ApiOperation(value ="업로드 취소" , notes = "파일 업로드가 취소 됩니다\n해당파일을 제외하고 업로드 할 시 사용합니다\n파라미터는 파일의 id 입니다.")
    public @ResponseBody
    ResponseEntity<Long> cancelUpload(@PathVariable Long id) throws Exception {
        lectureBankService.cancelUpload(id);
        return new ResponseEntity(HttpStatus.OK);
    }


    //check if file is users's? ************************************************************************


    @RequestMapping(value = "/file/download/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="단일 파일 다운로드" , notes = "파일을 1개 다운로드 합니다.\n파라미터는 파일의 id 입니다.")
    public @ResponseBody
    ResponseEntity getFile(@ApiParam(required = true) @PathVariable Long id) throws Exception {
        org.springframework.core.io.Resource resource = lectureBankService.getprivateObject(id);
        //if(resource == null) return new ResponseEntity(HttpStatus.OK);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType=null;
        try{
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        }catch (IOException e){
            System.out.println("default type null");
        }

        if(contentType == null){
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() +"\"")
                .body(resource);
    }


    //구매------------------------------------------------------------------------------------


    @Auth
    @RequestMapping(value = "/purchase/check/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 구매 여부" , notes = "유저가 자료를 구매하였는지 확인힙니다\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity<Boolean> checkPurchase(@PathVariable Long id) throws Exception{
        return new ResponseEntity<Boolean>(lectureBankService.checkPurchase(id), HttpStatus.OK);
    }

    @Auth
    @RequestMapping(value = "/purchase/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 구매하기" , notes = "강의 자료를 구매합니다\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity purchase(@PathVariable Long id) throws Exception{
        lectureBankService.purchase(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    //comment------------------------------------------------------------------------------------


    @RequestMapping(value = "/comments/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 댓글 불러오기" , notes = "강의자료 댓글 전체 조회\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity getComments(@PathVariable Long id) throws Exception{
        return new ResponseEntity<List<LectureBankComment>>(lectureBankService.getComments(id), HttpStatus.OK);
    }

    @Auth
    @RequestMapping(value = "/comment/{id}", method = RequestMethod.POST)
    @ApiOperation(value ="강의자료 댓글 작성" , notes = "강의자료 댓글을 입력합니다\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity addComment(@PathVariable Long id, @RequestParam(value = "comments") String comments) throws Exception{
        lectureBankService.addComment(id, comments);
        return new ResponseEntity(HttpStatus.OK);
    }


    @Auth
    @RequestMapping(value = "/comment/modify/{id}", method = RequestMethod.PATCH)
    @ApiOperation(value ="강의자료 댓글 수정" , notes = "강의자료 댓글을 수정합니다\n파라미터는 댓글 id 입니다.")
    public @ResponseBody
    ResponseEntity setComment(@PathVariable Long id, @RequestParam(value = "comments") String comments) throws Exception{
        lectureBankService.setComment(id, comments);
        return new ResponseEntity(HttpStatus.OK);
    }


    @Auth
    @RequestMapping(value = "/comment/delete/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value ="강의자료 댓글 삭제" , notes = "강의자료 댓글을 삭제합니다\n파라미터는 댓글 id 입니다.")
    public @ResponseBody
    ResponseEntity deleteComment(@PathVariable Long id) throws Exception{
        lectureBankService.deleteComment(id);
        return new ResponseEntity(HttpStatus.OK);
    }


    //hit------------------------------------------------------------------------------------

    @Auth
    @RequestMapping(value = "/hit/check/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="강의자료 hit 눌렀는지 체크" , notes = "유저가 hit를 눌렀는지 확인합니다\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity<Boolean> checkHit(@PathVariable Long id) throws Exception{
        return new ResponseEntity<Boolean>(lectureBankService.checkHits(id), HttpStatus.OK);
    }


    @Auth
    @RequestMapping(value = "/hit/push/{id}", method = RequestMethod.GET)
    @ApiOperation(value ="hit 누르기" , notes = "hit를 누릅니다\n파라미터는 강의 자료 id 입니다.")
    public @ResponseBody
    ResponseEntity pushHit(@PathVariable Long id) throws Exception{
        lectureBankService.pushHit(id);
        return new ResponseEntity(HttpStatus.OK);
    }


    //신고하기------------------------------------------------------------------------------------



}
