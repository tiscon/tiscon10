package com.tiscon10.viewhelper;

import java.io.IOException;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

/**
 * Spring MVCでHandlebars.javaを使用するためのヘルパークラス。
 *
 * {@link BINDING_RESULT_KEY}, {@link HasFieldErrorsHelper}, {@link FieldErrorsHelper} を組み合わせて
 * Handlebars側でのエラーの表示を1つずつ個別に表示させたい場合に使用することができる。
 *
 * 以下に、入力画面における個別エラー表示の例を示す。
 * なお、どの画面のどの部分にエラーを表示させたいかによって、実装例の一部の内容は異なるため、注意すること。
 *
 * Controllerの実装例
 * if (result.hasErrors()) {
 *   // エラーを表示するために、BindingResultをModelに格納する
 *   model.addAttribute(SpringMVCHelper.BINDING_RESULT_KEY, result);
 *   return "input";
 * }
 *
 * Handlebarsの実装例
 * <label>
 *   氏名
 *   <input type="text" name="kanjiName" value="{{userOrderForm.kanjiName}}"/>
 * </label>
 * {#hasFieldErrors "kanjiName"}}
 *    <ul>
 *      {{#fieldErrors "kanjiName"}}<li>{{this}}</li>{{/fieldErrors}}
 *    </ul>
 * {{/hasFieldErrors}}
 */
public class SpringMVCHelper {

    /**
     * BindingResultをHandlebarsで扱うためのキー。
     * Controllerで、ModelにBindingResultを設定する際に使用する。
     *
     */
    public static final String BINDING_RESULT_KEY = "result";

    /**
     * {@link BindingResult#hasFieldErrors(String)}をHandlebarsで扱うためのHelper。
     *
     * 引数で指定したフィールドにエラーがあるかどうかをチェックする。
     * 指定した項目にエラーがある場合は、ブロック内の処理が実行される。
     *
     * Handlebarsにおける使用例
     * {{#hasFieldErrors "kanjiName"}}
     *    <!-- エラーがある場合に、ここに書いた処理が実行される -->
     * {{/hasFieldErrors}}
     *
     */
    public static class HasFieldErrorsHelper implements Helper<String> {

        /**
         * @param field   エラー項目のフィールド名
         * @param options Handlebarsのオプション
         * @return 適用した結果（HTML）
         */
        @Override
        public Object apply(String field, Options options) throws IOException {
            BindingResult result = (BindingResult) options.context.get(BINDING_RESULT_KEY);
            if (result != null && result.hasFieldErrors(field)) {
                return options.fn();
            }
            return options.inverse();
        }
    }


    /**
     * {@link BindingResult#getFieldErrors(String)}をHandlebarsで扱うためのHelper。
     *
     * 引数で指定したフィールドのエラー情報を出力する。
     *
     * 指定した項目にエラーがある場合は、ブロック内の処理が実行される。
     * エラーメッセージは、{@code this}で出力できる。
     * エラーが複数ある場合は、ループで1つずつ出力される。
     *
     * Handlebarsにおける使用例
     * {{#fieldErrors "kanjiName"}}<li>{{this}}</li>{{/fieldErrors}}
     *
     * エラーが2件ある場合は以下のようにレンダリングされる。
     *
     * <span>10文字以内で入力してください</span>
     * <span>カタカナで入力してください</span>
     *
     */
    public static class FieldErrorsHelper implements Helper<String> {

        /**
         * @param field   エラー項目のフィールド名
         * @param options Handlebarsのオプション
         * @return 適用した結果（HTML）
         */
        @Override
        public Object apply(String field, Options options) throws IOException {
            BindingResult result = (BindingResult) options.context.get(BINDING_RESULT_KEY);
            if (result == null || !result.hasFieldErrors(field)) {
                // エラーがなければなにもしない
                return options.inverse();
            }
            // 項目に紐づくエラーを取得
            List<FieldError> fieldErrors = result.getFieldErrors(field);
            StringBuilder errorMessages = new StringBuilder();
            for (FieldError fieldError : fieldErrors) {
                // 1エラーずつ評価する
                String errorMessage = fieldError.getDefaultMessage();
                CharSequence applied = options.fn(errorMessage);
                errorMessages.append(applied);
            }
            return errorMessages.toString();
        }

    }
}
