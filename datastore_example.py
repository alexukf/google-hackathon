


import logging
import sys
import json

import googledatastore as datastore


def get_data(dataset_id):

    # Set the dataset from the command line parameters.
    datastore.set_options(dataset=dataset_id)
    try:
        req = datastore.RunQueryRequest()
        query = req.query
        query.kind.add().name = 'Hackathlon'

        results = datastore.run_query(req).batch.entity_result

        # results[0].entity.property[0].value.string_value
        useful_res = []
        for res in results:
            ent = res.entity
            dict_res = {}

            for prop in ent.property:
                dict_res[prop.name] = prop.value.string_value
            useful_res.append(dict_res)

        return json.dumps(useful_res)

    except datastore.RPCError as e:
        logging.error('Error while doing datastore operation')
        logging.error('RPCError: %(method)s %(reason)s',
                      {'method': e.method, 'reason': e.reason})
        logging.error('HTTPError: %(status)s %(reason)s',
                      {'status': e.response.status,
                       'reason': e.response.reason})
        return

if __name__ == '__main__':
    main()
