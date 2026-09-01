package com.jian.jianpicturebackend.api.imagesearch;

import com.jian.jianpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.jian.jianpicturebackend.api.imagesearch.sub.GetBingImageListApi;

import java.util.List;

public class ImageSearchApiFacade {

    /**
     * 以图搜图
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        return GetBingImageListApi.getImageList(imageUrl);
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageList = searchImage("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
        System.out.println("结果列表" + imageList);
    }
}
