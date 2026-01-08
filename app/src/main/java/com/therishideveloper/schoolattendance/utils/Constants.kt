package com.therishideveloper.schoolattendance.utils

import com.therishideveloper.schoolattendance.R

sealed class IdTypes(val code: String, val stringRes: Int) {
    object NID : IdTypes("NID", R.string.id_type_nid)
    object BIRTH : IdTypes("BIRTH", R.string.id_type_birth)
    object NONE : IdTypes("NONE", R.string.id_type_none)

    companion object {
        fun getAll(): List<IdTypes> = listOf(NID, BIRTH, NONE)

        fun fromCode(code: String): IdTypes = when (code) {
            "NID" -> NID
            "BIRTH" -> BIRTH
            else -> NONE
        }
    }
}

sealed class ReligionTypes(val code: String, val stringRes: Int) {
    object ISLAM : ReligionTypes("ISLAM", R.string.religion_islam)
    object HINDU : ReligionTypes("HINDU", R.string.religion_hindu)
    object BUDDHIST : ReligionTypes("BUDDHIST", R.string.religion_buddhist)
    object CHRISTIAN : ReligionTypes("CHRISTIAN", R.string.religion_christian)
    object OTHERS : ReligionTypes("OTHERS", R.string.religion_others)

    companion object {
        fun getAll(): List<ReligionTypes> = listOf(ISLAM, HINDU, BUDDHIST, CHRISTIAN, OTHERS)

        fun fromCode(code: String): ReligionTypes = when (code) {
            "ISLAM" -> ISLAM
            "HINDU" -> HINDU
            "BUDDHIST" -> BUDDHIST
            "CHRISTIAN" -> CHRISTIAN
            else -> OTHERS
        }
    }
}

sealed class BloodGroupTypes(val code: String, val stringRes: Int) {
    object UNKNOWN : BloodGroupTypes("UNKNOWN", R.string.option_unknown)
    object A_POSITIVE : BloodGroupTypes("A+", R.string.bg_a_pos)
    object A_NEGATIVE : BloodGroupTypes("A-", R.string.bg_a_neg)
    object B_POSITIVE : BloodGroupTypes("B+", R.string.bg_b_pos)
    object B_NEGATIVE : BloodGroupTypes("B-", R.string.bg_b_neg)
    object O_POSITIVE : BloodGroupTypes("O+", R.string.bg_o_pos)
    object O_NEGATIVE : BloodGroupTypes("O-", R.string.bg_o_neg)
    object AB_POSITIVE : BloodGroupTypes("AB+", R.string.bg_ab_pos)
    object AB_NEGATIVE : BloodGroupTypes("AB-", R.string.bg_ab_neg)

    companion object {
        fun getAll(): List<BloodGroupTypes> = listOf(
            UNKNOWN, A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
            O_POSITIVE, O_NEGATIVE, AB_POSITIVE, AB_NEGATIVE
        )

        fun fromCode(code: String): BloodGroupTypes = when (code) {
            "A+" -> A_POSITIVE
            "A-" -> A_NEGATIVE
            "B+" -> B_POSITIVE
            "B-" -> B_NEGATIVE
            "O+" -> O_POSITIVE
            "O-" -> O_NEGATIVE
            "AB+" -> AB_POSITIVE
            "AB-" -> AB_NEGATIVE
            else -> UNKNOWN
        }
    }
}

sealed class GenderTypes(val code: String, val stringRes: Int) {
    object MALE : GenderTypes("MALE", R.string.gender_male)
    object FEMALE : GenderTypes("FEMALE", R.string.gender_female)
    object OTHERS : GenderTypes("OTHERS", R.string.gender_other)

    companion object {
        fun getAll(): List<GenderTypes> = listOf(MALE, FEMALE, OTHERS)

        fun fromCode(code: String): GenderTypes = when (code) {
            "MALE" -> MALE
            "FEMALE" -> FEMALE
            else -> OTHERS
        }
    }
}

sealed class ClassTypes(val code: String, val stringRes: Int) {
    object PLAY : ClassTypes("PLAY", R.string.class_play)
    object CLASS1 : ClassTypes("CLASS1", R.string.class_1)
    object CLASS2 : ClassTypes("CLASS2", R.string.class_2)
    object CLASS3 : ClassTypes("CLASS3", R.string.class_3)
    object CLASS4 : ClassTypes("CLASS4", R.string.class_4)
    object CLASS5 : ClassTypes("CLASS5", R.string.class_5)
    object CLASS6 : ClassTypes("CLASS6", R.string.class_6)
    object CLASS7 : ClassTypes("CLASS7", R.string.class_7)
    object CLASS8 : ClassTypes("CLASS8", R.string.class_8)

    companion object {
        fun getAll(): List<ClassTypes> = listOf(
            PLAY, CLASS1, CLASS2, CLASS3, CLASS4, CLASS5, CLASS6, CLASS7, CLASS8
        )

        fun fromCode(code: String): ClassTypes = when (code) {
            "PLAY" -> PLAY
            "CLASS1" -> CLASS1
            "CLASS2" -> CLASS2
            "CLASS3" -> CLASS3
            "CLASS4" -> CLASS4
            "CLASS5" -> CLASS5
            "CLASS6" -> CLASS6
            "CLASS7" -> CLASS7
            "CLASS8" -> CLASS8
            else -> PLAY
        }
    }
}

sealed class CountryTypes(val code: String, val stringRes: Int, val flag: String) {
    object BANGLADESH : CountryTypes("BD", R.string.country_bd, "🇧🇩")
    object INDIA : CountryTypes("IN", R.string.country_in, "🇮🇳")

    companion object {
        fun getAll(): List<CountryTypes> = listOf(BANGLADESH, INDIA)

        fun fromCode(code: String): CountryTypes = when (code) {
            "BD" -> BANGLADESH
            "IN" -> INDIA
            else -> BANGLADESH
        }
    }
}