from optparse import OptionParser
import os


if __name__ == '__main__':
	parser = OptionParser()
	parser.add_option("-a", "--algorithm",
                  action="store", type="string", dest="algorithm")
	parser.add_option("-d", "--dataset",
				  action="store", type="string", dest="dataset")
	(options, args) = parser.parse_args()

	alg_path = os.path.abspath(options.algorithm)
	data_path = os.path.abspath(options.dataset)

	alg_dir = os.path.dirname(alg_path)
	os.sys.path.append(alg_dir)
	from setup import QA

	errors = []
	correct = []
	num_tests = 0
	num_correct = 0
	with open(data_path, "r") as data:
		for line in data:
			try:
				question = line.split('\t')[0]
				answer = line.split('\t')[1][:-2].lower()

				num_tests += 1
				prediction = QA(question)
				# print question, answer
				if prediction is None:
					continue

				if prediction.lower() in answer or answer in prediction.lower():
					correct.append((question, answer, prediction))
					num_correct += 1
			except Exception,e:
				errors.append((question, answer))


	print 'Number of Tests: %d' % (num_tests)
	print 'Number Correct: %d' %(num_correct)
	print 'Percent Correct: %f' % (float(num_correct) / num_tests)

	print correct