package com.tiscon10.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiscon10.dao.EstimateDao;
import com.tiscon10.domain.InsuranceOrder;
import com.tiscon10.domain.InsuranceType;

/**
 * 保険見積もり機能において業務処理を担当するクラス。
 *
 * @author TIS Taro
 */
@Service
public class EstimateService {

    /**
     * 見積もりDAO
     */
    @Autowired
    private EstimateDao estimateDAO;

    /**
     * 保険種別テーブルに登録されているすべての保険種別を取得する。
     *
     * @return すべての保険種別
     */
    public List<InsuranceType> getInsurances() {
        return estimateDAO.getAllInsurances();
    }

    /**
     * 保険種別名を取得する。
     *
     * @param insuranceType 保険種別タイプ
     * @return 保険種別名
     */
    public String findInsuranceName(Integer insuranceType) {
        return estimateDAO.findInsuranceName(insuranceType);
    }

    /**
     * 生年月日と保険種別から保険料（年額）の見積もりを算出する。
     *
     * @param insuranceType 保険種別タイプ
     * @param dateOfBirth 生年月日
     * @return 見積もり結果
     */
    public EstimateResult calculateInsuranceFee(Integer insuranceType, LocalDate dateOfBirth) {
        // ユーザーが選択した保険種別の月額保険料を取得する。
        int monthlyFee = estimateDAO.findMonthlyFee(insuranceType);

        // ユーザーが選択した生年月日と現在日付から年齢を取得する。
        int age = calculateAge(dateOfBirth);

        // 年齢による調整率を取得する。
        double adjustmentRateByAge = estimateDAO.findAdjustmentRateByAge(age);

        // 保険料（年額）を計算する。
        int annualFee = (int) (monthlyFee * 12 * adjustmentRateByAge);

        // 見積もり結果を返す。
        EstimateResult estimateResult = new EstimateResult(annualFee, adjustmentRateByAge, age);
        return estimateResult;

    }

    /**
     * 生年月日と現在日付から年齢を計算し、年齢が20歳以上100歳以下であるかを判定する。
     *
     * @param dateOfBirth 生年月日
     * @return 年齢が20歳以上100歳以下である場合、真
     */
    public boolean isAgeValid(LocalDate dateOfBirth) {
        int age = calculateAge(dateOfBirth);
        return age >= 20 && age <= 100;
    }

    /**
     * 生年月日と現在日付から年齢を計算する。
     *
     * @param dateOfBirth 生年月日
     * @return 現在の年齢
     */
    private int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(dateOfBirth, currentDate);
        return period.getYears();
    }

    /**
     * データベースに見積もり依頼を登録する。
     *
     * @param insuranceOrder 見積もり依頼情報
     */
    @Transactional
    public void registerOrder(InsuranceOrder insuranceOrder) {
        estimateDAO.insertInsuranceOrder(insuranceOrder);
    }
}
