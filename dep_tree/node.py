# node.py

class node(object):
	def __init__(self, w, p, r, c):
		self.word = w 
		self.parent = p
		self.relation = r
		self.children = c

