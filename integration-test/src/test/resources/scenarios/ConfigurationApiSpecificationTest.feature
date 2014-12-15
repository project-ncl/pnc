Feature: Specifies example usage of Project's Configuration REST interface.
  Included examples:
    - Project's configuration REST validation

  Scenario Outline: Validating Project Configuration with REST interface
    Given Standard list of Projects configurations
    When Adding a Configuration with <Identifier>, <BuildScript> and <ScmUrl>
    Then API return status is <HttpReturnCode>
  Examples:
    | Identifier | BuildScript |       ScmUrl       | HttpReturnCode |
    | test       | test        | http://google.com  | 201            |
    | 'null'     | test        | http://google.com  | 400            |
    | test       | 'null'      | http://google.com  | 400            |
    | test       | test        | 'null'             | 400            |