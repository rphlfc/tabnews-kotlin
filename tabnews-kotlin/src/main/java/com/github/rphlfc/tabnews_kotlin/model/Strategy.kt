package com.github.rphlfc.tabnews_kotlin.model

enum class Strategy(
    val title: String,
    val param: String
) {
    RELEVANT(
        title = "Relevantes",
        param = "relevant"
    ),
    NEW(
        title = "Recentes", 
        param = "new"
    )
}
