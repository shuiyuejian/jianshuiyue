package com.jian.jianpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jian.jianpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.jian.jianpicturebackend.exception.BusinessException;
import com.jian.jianpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 Bing 视觉搜索的以图搜图（百度 graph.baidu.com 接口已封禁匿名请求，改用 Bing）
 */
@Slf4j
public class GetBingImageListApi {

    /**
     * Bing 按图 URL 搜索：q=imgurl:图片地址
     */
    private static final String BING_SEARCH_URL = "https://www.bing.com/images/search?view=detailv2&iss=sbi&form=SBIVSP&sbisrc=UrlShare&q=imgurl:%s&idpbck=1&selectedindex=0";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private static final int MAX_REDIRECT_HOPS = 5;

    /**
     * 每个结果是一条 <a class="iusc" ... m="{...}">，m 属性为 HTML 实体编码的 JSON，
     * 其中 murl=原图地址、turl=缩略图地址、purl=来源页面地址
     */
    private static final Pattern IUSC_TAG_PATTERN = Pattern.compile("<a[^>]*class=\"iusc\"[^>]*>");
    private static final Pattern M_ATTR_PATTERN = Pattern.compile("m=\"([^\"]*)\"");

    /**
     * 获取识图结果列表
     *
     * @param imageUrl 待搜索的图片地址
     * @return 识图结果列表
     */
    public static List<ImageSearchResult> getImageList(String imageUrl) {
        try {
            String url = String.format(BING_SEARCH_URL, encodeQueryValue(imageUrl));
            HttpResponse response = getWithRedirects(url);
            String body = response.body();
            if (response.getStatus() != 200 || StrUtil.isBlank(body)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            return parseImageList(body);
        } catch (Exception e) {
            log.error("调用 Bing 以图搜图接口失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    /**
     * 编码 query 值，URLEncoder 会把空格编为 +，query 中应使用 %20
     */
    private static String encodeQueryValue(String value) throws java.io.UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    }

    /**
     * 发起 GET 并手动跟随重定向（Hutool 的 setFollowRedirects 对 bing.com 的跳转不生效）
     */
    private static HttpResponse getWithRedirects(String url) {
        HttpResponse response = null;
        try {
            String currentUrl = url;
            for (int i = 0; i < MAX_REDIRECT_HOPS; i++) {
                response = HttpRequest.get(currentUrl)
                        .header("User-Agent", USER_AGENT)
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .timeout(10000)
                        .execute();
                String location = response.header("Location");
                if (isRedirect(response.getStatus()) && StrUtil.isNotBlank(location)) {
                    String absoluteLocation = URI.create(currentUrl).resolve(location).toString();
                    response.close();
                    response = null;
                    currentUrl = absoluteLocation;
                    continue;
                }
                return response;
            }
            return response;
        } catch (Exception e) {
            if (response != null) {
                response.close();
            }
            throw e;
        }
    }

    private static boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    /**
     * 解析 Bing 结果页中的 iusc 结果项
     */
    private static List<ImageSearchResult> parseImageList(String body) {
        List<ImageSearchResult> resultList = new ArrayList<>();
        Matcher tagMatcher = IUSC_TAG_PATTERN.matcher(body);
        while (tagMatcher.find()) {
            String tag = tagMatcher.group();
            Matcher mAttrMatcher = M_ATTR_PATTERN.matcher(tag);
            if (!mAttrMatcher.find()) {
                continue;
            }
            String mJson = HtmlUtil.unescape(mAttrMatcher.group(1));
            if (StrUtil.isBlank(mJson)) {
                continue;
            }
            try {
                JSONObject jsonObject = JSONUtil.parseObj(mJson);
                String murl = jsonObject.getStr("murl");
                String purl = jsonObject.getStr("purl");
                if (StrUtil.isBlank(murl) && StrUtil.isBlank(purl)) {
                    continue;
                }
                ImageSearchResult imageSearchResult = new ImageSearchResult();
                imageSearchResult.setThumbUrl(StrUtil.isNotBlank(murl) ? murl : jsonObject.getStr("turl"));
                imageSearchResult.setFromUrl(StrUtil.isNotBlank(purl) ? purl : murl);
                resultList.add(imageSearchResult);
            } catch (Exception ignore) {
                // 单条结果解析失败则跳过
            }
        }
        return resultList;
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageList = getImageList("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
        System.out.println("结果条数：" + imageList.size());
        for (ImageSearchResult result : imageList) {
            System.out.println(result);
        }
    }
}
