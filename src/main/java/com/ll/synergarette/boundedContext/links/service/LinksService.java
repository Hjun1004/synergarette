package com.ll.synergarette.boundedContext.links.service;

import com.ll.synergarette.base.rq.Rq;
import com.ll.synergarette.base.rsData.RsData;
import com.ll.synergarette.boundedContext.links.controller.LinksController;
import com.ll.synergarette.boundedContext.links.entity.LinkForm;
import com.ll.synergarette.boundedContext.links.entity.Links;
import com.ll.synergarette.boundedContext.links.repository.LinksRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 아래 메서드들이 전부 readonly 라는 것을 명시, 나중을 위해
public class LinksService {
    private final Rq rq;

    private final LinksRepository linksRepository;

    public RsData findById(Long id) {
        Links links = linksRepository.findById(id).orElse(null);

        if(links == null){
            return RsData.of("F-1", "없는 링크 입니다.");
        }

        return RsData.of("S-1", "링크를 찾았습니다.", links);
    }

    @Transactional
    public RsData deleteLinks(Links links) {
        linksRepository.delete(links);

        return RsData.of("S-1", "링크가 삭제되었습니다.");
    }


    @Transactional
    public RsData<Links> modifyLinks(Links links, LinkForm linkForm, String urlLinks, String linksName) {
        links.setLinksName(linksName);
        links.setUrlLinks(urlLinks);

        String youtubeVideoCheck = youtubeVideoCheck(urlLinks);
        String brandCheck = brandCheck(urlLinks);
        String realImageUrls = checkRealImageUrl(linkForm.getImageUrl()); // 이미지를 업로드 했는지 체크하고 이미지가 있으면 이미지의 url을 가져온다.

        if(realImageUrls != null && links.getImageUrl() != linkForm.getImageUrl()){
            links.setImageUrl(realImageUrls);
        }else{
            links.setImageUrl(null);

            if(links.getBrand() != brandCheck){ // 링크의 브랜드가 변경 됐으면 변경된 브랜드 저장
                links.setBrand(brandCheck);
            }

            if(youtubeVideoCheck != null){ // 새로 등록된 링크가 유뷰트 영상 링크면 썸네일 이미지 저장
                links.setImageUrl(youtubeVideoCheck);
            }
        }


        return RsData.of("S-1", "수정되었습니다.", links);
    }

    public RsData canModify(Links links) {
        if(links == null){
            return RsData.of("F-1", "존재하지 않는 링크 입니다.");
        }

        if(!rq.isAdmin()){
            return RsData.of("F-2", "관리자만이 수정할 수 있습니다.");
        }

        return RsData.of("S-1", "수정 가능 합니다.");
    }

    public RsData<Links> registrationLinks(LinkForm linkForm) {
        String realImageUrls = checkRealImageUrl(linkForm.getImageUrl()); // 이미지를 업로드 했는지 체크하고 이미지가 있으면 이미지의 url을 가져온다.

        if(realImageUrls != null){ // 이미지를 등록한다면, 이미지를 직접 저장
            return registrationLinks(linkForm.getLinksName(), linkForm.getUrlLinks(), realImageUrls);
        }else return registrationLinks(linkForm.getLinksName(), linkForm.getUrlLinks());
    }

    @Transactional
    public RsData<Links> registrationLinks(String linksName, String urlLinks) {
        // 등록된 링크가 유튜브 영상인지 확인 -> 유튜브 영상이면 썸네일 이미지 주소를 저장
        String youtubeVideoCheck = youtubeVideoCheck(urlLinks);

        // 유튜브 영상 링크에 따로 이미지 url이 없으면 무조건 유튜브의 영상 썸네일을 표기하기로
        if(youtubeVideoCheck != null){
            return registrationLinks(linksName, urlLinks, youtubeVideoCheck);
        }

        return registrationLinks(linksName, urlLinks, null);
    }

    @Transactional
    public RsData<Links> registrationLinks(String linksName, String urlLinks, String imageUrl){

        String brandCheck = brandCheck(urlLinks);

        Links links = Links
                .builder()
                .linksName(linksName)
                .urlLinks(urlLinks)
                .imageUrl(imageUrl)
                .brand(brandCheck)
                .build();

        linksRepository.save(links);

        return RsData.of("S-1", "url저장 완료", links);
    }

    private String checkRealImageUrl(String imageUrl) {
        if(!imageUrl.contains("[image alt attribute]")){
            return null;
        }else {
            String realImage[] = imageUrl.split("e]");
            int a = realImage[1].indexOf(')');
            String realImageUrl = realImage[1].substring(1,a);

            return realImageUrl;
        }
    }

    public List<Links> findAll() {
        return linksRepository.findAll();
    }

    public List<Links> findAllReverse(){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        return linksRepository.findAll(sort);
    }

    private String youtubeVideoCheck(String urlLinks){

        if(urlLinks.contains("youtube.com")){
            if(urlLinks.contains("watch?v=")){
                String urls[]  = urlLinks.split("v=");

                if(urls[1] != null){

                    String youtubeThumbnail = "https://img.youtube.com/vi/" + urls[1] + "/0.jpg";

                    return youtubeThumbnail;
                }

            }
        }
        return null;
    }


    private String brandCheck(String urlLinks) {

        if(urlLinks.contains("youtube.com")){
            return "youtube";
        }else if(urlLinks.contains("instagram.com")){
            return "instagram";
        }else if(urlLinks.contains("melon.com")){
            return "melon";
        }else if(urlLinks.contains("music.apple.com")){
            return "appleMusic";
        }else if(urlLinks.contains("genie.co.kr")){
            return "genie";
        }else if(urlLinks.contains("spotify.com")){
            return "spotify";
        }else if(urlLinks.contains("music.youtube")){
            return "youtubeMusic";
        }else if(urlLinks.contains("music-flo")){
            return "flo";
        }else if(urlLinks.contains("vibe.naver.com")){
            return "vibe";
        }else if(urlLinks.contains("music.bugs")){
            return "bugs";
        }

        return null;
    }


}
