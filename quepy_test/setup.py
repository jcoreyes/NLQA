__author__ = 'coreyesj'

# Set up quepy api
from os import sys, path
qtestPath = path.dirname(path.dirname(path.abspath(__file__))) + "/quepy_test/quepy_api"
sys.path.append(qtestPath)
from main import query
import quepy
from SPARQLWrapper import SPARQLWrapper, JSON

sparql = SPARQLWrapper("http://dbpedia.org/sparql")
dbpedia = quepy.install("app")

def QA(question):
    # default_questions = [
    #     "Who killed Abraham Lincoln?"
    # ]
    # default_questions = [
    #     "What is a car?",
    #     "Who is Tom Cruise?",
    #     "Who is George Lucas?",
    #     "Who is Mirtha Legrand?",
    #     # "List Microsoft software",
    #     "Name Fiat cars",
    #     "time in argentina",
    #     "what time is it in Chile?",
    #     "List movies directed by Martin Scorsese",
    #     "How long is Pulp Fiction",
    #     "which movies did Mel Gibson starred?",
    #     "When was Gladiator released?",
    #     "who directed Pocahontas?",
    #     "actors of Fight Club",
    # ]


    result = query(question)
    return result
