package com.example.jobfinder.Datas.Model

class JobHistoryParentModel(val jobTitle:String,val jobType:String, val childernList:MutableList<JobHistoryModel>, var isExpanded:Boolean = false)