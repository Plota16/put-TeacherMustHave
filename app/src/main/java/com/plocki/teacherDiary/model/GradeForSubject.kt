package com.plocki.teacherDiary.model

class GradeForSubject(
    var id: Int,
    var weight: Int,
    var weightName: String,
    var gradeName: String,
    var gradeSymbol: String,
    var gradeValue: Double,
    var testId: Int?,
    var description: String = "",
    var date: String,
    var subjectForClass: Int
) {
}